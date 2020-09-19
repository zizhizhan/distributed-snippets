package com.zizhizhan.zk.usage;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Eamil: zhiqiangzhan@gmail.com
 * Date: 2019-01-04 17:19
 *
 * 原生 API 的一些问题：
 * 1. 如果父节点不存在，不能直接创建子节点
 * 2. 删除时，如果节点不存在，抛出错误
 * 3. 节点监控是一次性的，要一直监控，得自己处理
 * 4. 连接丢失，需自己处理，比如重连接
 */
@Slf4j
public class MonitorMain {

    /**
     * MonitorMain /my-test /tmp/my-test.txt cat /tmp/my-test.txt
     */
    public static void main(String[] args) {
        String connectString = "master:2181,master:2182,master:2183";

        if (args.length < 3) {
            System.err.println("USAGE: Executor znode filename program [args ...]");
            System.exit(2);
        }

        String znode = args[0];
        String filename = args[1];
        String[] exec = new String[args.length - 2];
        System.arraycopy(args, 2, exec, 0, exec.length);

        log.info("znode {} with exec {}.", znode, Joiner.on(",").join(exec));
        try {
            new Executor(connectString, znode, filename, exec).run();
        } catch (Exception e) {
            log.error("Unexpected Error.", e);
        }
    }

    public static class Executor implements Watcher, Runnable, DataMonitorListener {
        private final DataMonitor dataMonitor;
        private final String filename;
        private final String[] exec;
        private Process child;

        private Executor(String connectString, String znode, String filename, String[] exec) throws IOException {
            this.filename = filename;
            this.exec = exec;
            ZooKeeper zooKeeper = new ZooKeeper(connectString, 3000, this);
            this.dataMonitor = new DataMonitor(zooKeeper, znode, null, this);
        }

        @Override
        public void run() {
            try {
                synchronized (this) {
                    while (!dataMonitor.dead.get()) {
                        wait();
                    }
                }
            } catch (InterruptedException e) {
                log.info("IGNORE interrupt.", e);
            }
        }

        @Override
        public void exists(byte[] data) {
            if (data == null) {
                if (child != null) {
                    System.out.println("Killing process");
                    child.destroy();
                    try {
                        child.waitFor();
                    } catch (InterruptedException e) {
                        log.info("IGNORE interrupt.", e);
                    }
                }
                child = null;
            } else {
                if (child != null) {
                    System.out.println("Stopping child");
                    child.destroy();
                    try {
                        child.waitFor();
                    } catch (InterruptedException e) {
                        log.info("Unexpected interrupt.", e);
                    }
                }
                try {
                    FileOutputStream fos = new FileOutputStream(filename);
                    fos.write(data);
                    fos.close();
                } catch (IOException e) {
                    log.error("Unexpected IO Error.", e);
                }
                try {
                    log.info("Starting child");
                    child = Runtime.getRuntime().exec(exec);
                    new StreamWriter(child.getInputStream(), System.out);
                    new StreamWriter(child.getErrorStream(), System.err);
                } catch (IOException e) {
                    log.error("Unexpected IO Error.", e);
                }
            }
        }

        @Override
        public void closing(KeeperException.Code code) {
            synchronized (this) {
                notifyAll();
            }
        }

        @Override
        public void process(WatchedEvent event) {
            dataMonitor.process(event);
        }
    }


    public static class DataMonitor implements Watcher, AsyncCallback.StatCallback {
        private final AtomicBoolean dead = new AtomicBoolean(false);
        private final ZooKeeper zooKeeper;
        private final DataMonitorListener listener;
        private final Watcher chainedWatcher;
        private final String znode;
        private byte[] prevData;

        private DataMonitor(ZooKeeper zooKeeper, String znode, Watcher chainedWatcher, DataMonitorListener listener) {
            this.zooKeeper = zooKeeper;
            this.znode = znode;
            this.chainedWatcher = chainedWatcher;
            this.listener = listener;
            zooKeeper.exists(znode, true, this, null);
        }

        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            boolean exists;
            KeeperException.Code rcCode = KeeperException.Code.get(rc);
            switch (rcCode) {
                case OK:
                    exists = true;
                    break;
                case NONODE:
                    exists = false;
                    break;
                case SESSIONEXPIRED:
                case NOAUTH:
                    dead.set(false);
                    listener.closing(rcCode);
                    return;
                default:
                    zooKeeper.exists(znode, true, this, null);
                    return;
            }

            byte[] b = null;
            if (exists) {
                try {
                    b = zooKeeper.getData(znode, false, null);
                } catch (KeeperException e) {
                    log.info("Can't get data for path {}.", znode, e);
                } catch (InterruptedException e) {
                    return;
                }
            }
            if ((b == null && b != prevData) || (b != null && !Arrays.equals(prevData, b))) {
                listener.exists(b);
                prevData = b;
            }
        }

        @Override
        public void process(WatchedEvent event) {
            String path = event.getPath();
            log.info("{}: ({}, {})", event.getType(), path, event.getState());
            if (Event.EventType.None == event.getType()) {
                switch (event.getState()) {
                    case SyncConnected:
                        log.info("Zookeeper connected.");
                        dead.compareAndSet(true, false);
                        break;
                    case Disconnected:
                        log.info("Zookeeper disconnected.");
                        break;
                    case Expired:
                        log.info("Zookeeper expired.");
                        dead.compareAndSet(false, true);
                        listener.closing(KeeperException.Code.SESSIONEXPIRED);
                        break;
                    case AuthFailed:
                        log.info("Zookeeper auth failed.");
                        break;
                    default:
                        log.info("Zookeeper unknown operation.");
                        break;
                }
            } else {
                // if (path != null && path.equals(znode)) {
                zooKeeper.exists(path, true, this, null);
                // }
            }

            if (chainedWatcher != null) {
                chainedWatcher.process(event);
            }
        }
    }

    public interface DataMonitorListener {

        void exists(byte[] data);

        void closing(KeeperException.Code code);

    }

    static class StreamWriter extends Thread {
        private final OutputStream out;
        private final InputStream in;

        private StreamWriter(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
            start();
        }

        public void run() {
            byte[] buf = new byte[80];
            int rc;
            try {
                while ((rc = in.read(buf)) > 0) {
                    out.write(buf, 0, rc);
                }
            } catch (IOException e) {
                log.error("Unexpected IO Error.", e);
            }
        }
    }

}
