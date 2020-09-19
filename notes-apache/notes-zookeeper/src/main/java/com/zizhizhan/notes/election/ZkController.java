package com.zizhizhan.notes.election;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZkController {

    private static final String CONTROLLER = "/controller";

    private final String brokerId;
    private final String connectString;

    private ZooKeeper zkClient;
    private final Watcher watcher = e -> {
        log.info("Get event {} for {}.", CONTROLLER, e);
        doElection();
    };

    public ZkController(String brokerId, String connectString) {
        this.brokerId = brokerId;
        this.connectString = connectString;
    }

    private void doElection() {
        try {
            if (this.zkClient == null) {
                this.zkClient = connect(this.connectString);
            }
            Stat stat = zkClient.exists(CONTROLLER, watcher);
            if (stat == null) {
                JSONObject info = new JSONObject();
                info.put("version", Math.floor(Math.random() * 10));
                info.put("brokerId", brokerId);
                info.put("timestamp", System.currentTimeMillis());
                String data = info.toString();
                log.info("no controller exists, try to be the controller with {}.", data);
                this.zkClient.create(CONTROLLER, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            } else {
                log.info("controller exists with {}.", stat);
                byte[] dataBytes = zkClient.getData(CONTROLLER, false, null);
                log.info("Current controller is {}.", new String(dataBytes));
            }
        } catch (Exception e) {
            log.info("Unexpected error.", e);
        }
    }

    public static void main(String[] args) {
        ZkController zkController = new ZkController(args[0], "localhost:2181");
        new Thread(() -> {
            zkController.doElection();
            while (true) {
                // 模拟执行其他操作
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static ZooKeeper connect(String connectString) throws IOException, InterruptedException {
        log.info("Try to connect {}.", connectString);
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(connectString, 3000, e -> {
            switch (e.getState()) {
                case Closed:
                    log.info("{} connected.", e);
                    break;
                case Expired:
                    log.info("{} expired.", e);
                    break;
                case Disconnected:
                    log.info("{} disconnected.", e);
                    break;
                case AuthFailed:
                    log.info("{} auth failed.", e);
                    break;
                case SaslAuthenticated:
                    log.info("{} sasl authenticated.", e);
                    break;
                case SyncConnected:
                    log.info("{} sync connected.", e);
                    latch.countDown();
                    break;
                case ConnectedReadOnly:
                    log.info("{} connected readOnly.", e);
                    break;
                default:
                    log.info("unknown {}.", e);
            }
        });
        latch.await();
        return zooKeeper;
    }
}
