package com.zizhizhan.zk.usage;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import static org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-01-04 17:59
 *
 * 原生 API 的一些问题：
 * 1. 如果父节点不存在，不能直接创建子节点
 * 2. 删除时，如果节点不存在，抛出错误
 * 3. 节点监控是一次性的，要一直监控，得自己处理
 * 4. 连接丢失，需自己处理，比如重连接
 */
@Slf4j
public class CRUDNodeTests {

    private static final String CONNECT_STRING = "master:2181,master:2182,master:2183";

    private static ZooKeeper zooKeeper;

    @BeforeClass
    public static void connectZookeeper() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(CONNECT_STRING, 5000, event -> {
            if (SyncConnected.equals(event.getState())) {
                log.info("Zookeeper connected.");
                latch.countDown();
            } else {
                log.info("Zookeeper with event {}.", event);
            }
        });

        latch.await();
        log.info("Session is (id: {}, passwd: {}, timeout: {})", zooKeeper.getSessionId(),
                new String(zooKeeper.getSessionPasswd(), StandardCharsets.ISO_8859_1), zooKeeper.getSessionTimeout());
        zooKeeper.register(watcherOf("GLOBAL"));
    }

    @Test
    public void persistent() throws Exception {
        doTestCRUD("/persistent-test", "test-data", CreateMode.PERSISTENT);
    }

    @Test
    public void persistentSequential() throws Exception {
        doTestCRUD("/persistent-sequential-test", "test-data", CreateMode.PERSISTENT_SEQUENTIAL);
    }

    @Test
    public void ephemeral() throws Exception {
        doTestCRUD("/ephemeral-test", "test-data", CreateMode.EPHEMERAL);
    }

    @Test
    public void ephemeralSequential() throws Exception {
        doTestCRUD("/ephemeral-sequential-test", "test-data", CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    @AfterClass
    public static void disconnectZooKeeper() throws InterruptedException {
        log.info("Closing {}.", zooKeeper);
        zooKeeper.close();
    }

    private void doTestCRUD(String path, String data, CreateMode createMode) throws Exception {
        Stat stat = zooKeeper.exists(path, watcherOf("EXIST"));
        try {
            String originPath = path;
            if (stat == null) {
                byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
                path = zooKeeper.create(originPath, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("Create {} and get {}.", originPath, path);
            } else {
                log.info("Use existing node {} with stat {}.", path, statToString(stat));
            }
            log.info("Get data {}.", new String(zooKeeper.getData(path, watcherOf("GET"), null), StandardCharsets.UTF_8));
            zooKeeper.setData(path, (data + "_2").getBytes(StandardCharsets.UTF_8), -1);
            log.info("Get data {}.", new String(zooKeeper.getData(path, watcherOf("GET2"), null), StandardCharsets.UTF_8));
        } finally {
            log.info("delete node {}.", path);
            zooKeeper.delete(path, -1);
        }
    }

    private static String statToString(Stat stat) {
        StringBuilder sb = new StringBuilder();
        sb.append("czxid").append(':').append(stat.getCzxid()).append('\n')
            .append("mzxid").append(':').append(stat.getMzxid()).append('\n')
            .append("pzxid").append(':').append(stat.getPzxid()).append('\n')
            .append("version").append(':').append(stat.getVersion()).append('\n')
            .append("cversion").append(':').append(stat.getCversion()).append('\n')
            .append("aversion").append(':').append(stat.getAversion()).append('\n')
            .append("dataLength").append(':').append(stat.getDataLength()).append('\n')
            .append("numChildren").append(':').append(stat.getNumChildren()).append('\n');
        return sb.toString();
    }

    private static Watcher watcherOf(String label) {
        return e -> log.info("{}: {}", label, e);
    }
}
