package me.jameszhan.notes.zk.locks.v1;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-01-03
 * Time: 14:51
 */
@Slf4j
public class DistributedLock {

    private static final int DEFAULT_SESSION_TIMEOUT = 5000;
    private static final String ROOT_NODE = "distributed-lock";
    private static final String COMPETITOR_NODE = "competitor";
    private final static String SEPARATOR = "/";
    private static final byte[] EMPTY_DATA  = {};

    private final ZooKeeper zooKeeper;
    private final String lockName;
    private String competitorPath;

    public DistributedLock(String connectString, String lockName) {
        try {
            // 确保连接zk成功
            CountDownLatch latch = new CountDownLatch(1);
            this.zooKeeper = new ZooKeeper(connectString, DEFAULT_SESSION_TIMEOUT, event -> {
                if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
                    log.info("Connected to zookeeper server success.");
                    latch.countDown();
                }
            });
            latch.await();

            String rootPath = SEPARATOR + ROOT_NODE;
            rootPath = createIfAbsent(rootPath);

            String lockPath = rootPath + SEPARATOR + lockName;
            lockPath = createIfAbsent(lockPath);

            if (lockPath == null || lockPath.isEmpty()) {
                throw new RuntimeException("Lock create failure.");
            }

            this.lockName = lockName;
        } catch (IOException | KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean tryLock(){
        String rootPath = SEPARATOR + ROOT_NODE;
        String lockPath = rootPath + SEPARATOR + lockName;
        List<String> competitorList = null;
        try {
            String path = lockPath + SEPARATOR + COMPETITOR_NODE;
            competitorPath = zooKeeper.create(path, EMPTY_DATA, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            competitorList = zooKeeper.getChildren(lockPath, false);
        } catch (KeeperException e) {
            throw new RuntimeException("zookeeper connect error");
        } catch (InterruptedException e) {
            log.warn("Interrupt when tryLock.", e);
        }

        if (competitorPath != null && competitorList != null) {
            Collections.sort(competitorList);
            String competitorName = competitorPath.substring(competitorPath.lastIndexOf('/'));
            log.info("CompetitorName is {}.", competitorName);
            int index = competitorList.indexOf(competitorName);
            if (index == -1) {
                throw new RuntimeException("competitorPath not exist after create");
            }
            return (index == 0);
        } else {
            throw new RuntimeException("competitorPath not exist after create");
        }
    }

    public void lock() {
        String rootPath = SEPARATOR + ROOT_NODE;
        String lockPath = rootPath + SEPARATOR + lockName;
        List<String> competitorList = null;
        try {
            String path = lockPath + SEPARATOR + COMPETITOR_NODE;
            competitorPath = zooKeeper.create(path, EMPTY_DATA, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            competitorList = zooKeeper.getChildren(lockPath, false);
        } catch (KeeperException e) {
            throw new RuntimeException("zookeeper connect error");
        } catch (InterruptedException e) {
            log.warn("Interrupt when tryLock.", e);
        }

        if (competitorPath != null && competitorList != null) {
            log.info("Competitor path is {}.", competitorPath);
            Collections.sort(competitorList);
            String competitorName = competitorPath.substring(competitorPath.lastIndexOf('/') + 1);
            int index = competitorList.indexOf(competitorName);
            if (index == -1) {
                throw new RuntimeException("competitorPath not exist after create");
            } else if (index == 0) {
                return;
            } else {
                CountDownLatch waitLatch = new CountDownLatch(1);
                String prevCompetitorPath = lockPath + SEPARATOR + competitorList.get(index - 1);
                try {
                    Stat prevNodeStat = zooKeeper.exists(prevCompetitorPath, e -> {
                        if (e.getType().equals(Watcher.Event.EventType.NodeDeleted)
                                && e.getPath().equals(prevCompetitorPath)) {
                            waitLatch.countDown();
                        }
                    });

                    log.info("{} is waiting for {}.", competitorPath, prevCompetitorPath);
                    if (prevNodeStat == null) {
                        return;
                    } else {
                        waitLatch.await();
                    }
                } catch (KeeperException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new RuntimeException("competitorPath not exist after create");
        }
    }

    public void unlock() {
        if (competitorPath == null || competitorPath.isEmpty()) {
            log.error("competitorPath is empty.");
            return;
        }
        try {
            zooKeeper.delete(competitorPath, -1);
        } catch (KeeperException.NoNodeException e) {
            log.info("{} has already removed.", competitorPath);
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    private String createIfAbsent(String path) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path,false);
        if (stat == null) {
            try {
                return zooKeeper.create(path, EMPTY_DATA, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } catch (KeeperException.NodeExistsException e) {
                log.info("{} is already exist.", path, e);
            }
        }
        return path;
    }
}
