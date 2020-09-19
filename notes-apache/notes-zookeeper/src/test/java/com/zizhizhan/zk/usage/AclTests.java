package com.zizhizhan.zk.usage;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-01-05 23:43
 */
@Slf4j
public class AclTests {

    private static final String CONNECT_STRING = "master:2181,master:2182,master:2183";

    @Test(expected = KeeperException.NoAuthException.class)
    public void checkAclWithNoPermission() throws Exception {
        String path = "/acl-test";
        ZooKeeper zk1 = connectZookeeper();
        try {
            zk1.addAuthInfo("digest", "james".getBytes(UTF_8));
            zk1.create(path, "test-data".getBytes(UTF_8), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
            Assert.assertArrayEquals("test-data".getBytes(UTF_8), zk1.getData(path, false, null));

            ZooKeeper zk2 = connectZookeeper();
            zk2.getData(path, false, null);
            Assert.fail("This test case should throw AuthFailedException");
        } finally {
            zk1.delete(path, -1);
        }
    }

    @Test
    public void checkAclWithPermission() throws Exception {
        String path = "/acl-test";
        ZooKeeper zk1 = connectZookeeper();
        try {
            zk1.addAuthInfo("digest", "james".getBytes(UTF_8));
            zk1.create(path, "test-data".getBytes(UTF_8), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
            Assert.assertArrayEquals("test-data".getBytes(UTF_8), zk1.getData(path, false, null));

            List<ACL> acls = zk1.getACL(path, null);
            log.info("ACL for {} is {}.", path, acls);

            ZooKeeper zk2 = connectZookeeper();
            zk2.addAuthInfo("digest", "james".getBytes(UTF_8));
            zk2.getData(path, false, null);
        } finally {
            zk1.delete(path, -1);
        }
    }


    private ZooKeeper connectZookeeper() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(CONNECT_STRING, 5000, event -> {
            if (SyncConnected.equals(event.getState())) {
                log.info("Zookeeper connected.");
                latch.countDown();
            } else {
                log.info("Zookeeper with event {}.", event);
            }
        });

        latch.await();
        return zooKeeper;
    }

}
