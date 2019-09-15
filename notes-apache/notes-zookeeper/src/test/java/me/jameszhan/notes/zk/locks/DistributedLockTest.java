package me.jameszhan.notes.zk.locks;

import lombok.extern.slf4j.Slf4j;
import me.jameszhan.notes.zk.locks.v1.DistributedLock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-01-03
 * Time: 18:54
 */
@Slf4j
public class DistributedLockTest {

    private static int total = 0;

    public static void main(String[] args) throws InterruptedException {
        String connectString = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
        long timeStartMs = System.currentTimeMillis();
        int threadCount = 10;
        ExecutorService service = Executors.newFixedThreadPool(threadCount);

        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            service.execute(() -> {
                log.info("task {} prepared", Thread.currentThread().getName());
                DistributedLock lock = new DistributedLock(connectString, "test-locks");
                lock.lock();
                try {
                    log.info("task {} started", Thread.currentThread().getName());
                    for (int j = 0; j < 100000; j++) {
                        total++;
                    }
                } finally {
                    log.info("task {} finished", Thread.currentThread().getName());
                    lock.unlock();
                    latch.countDown();
                }
            });
        }
        latch.await();

        log.info("Total is {} cost {}ms.", total, System.currentTimeMillis() - timeStartMs);

        CountDownLatch latch2 = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            service.execute(() -> {
                log.info("task {} prepared", Thread.currentThread().getName());
                DistributedLock lock = new DistributedLock(connectString, "test-locks");
                log.info("task {} started", Thread.currentThread().getName());
                try {
                    int j = 0;
                    do {
                        if (lock.tryLock()) {
                            total++;
                            j++;
                        }
                    } while (j < 1000);
                } finally {
                    log.info("task {} finished", Thread.currentThread().getName());
                    lock.unlock();
                    latch2.countDown();
                }
            });
        }

        latch2.await();
        log.info("Total is {} cost {}ms.", total, System.currentTimeMillis() - timeStartMs);
        service.shutdown();
    }
}
