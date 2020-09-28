package com.zizhizhan.mvcc;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TransactionTests {

    private static final int CONCURRENCY_SIZE = 20;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(CONCURRENCY_SIZE);
    private static final Random randGen = new Random();

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(CONCURRENCY_SIZE);
        for (int i = 0; i < CONCURRENCY_SIZE; i++) {
            threadPool.execute(() -> {
                Transaction transaction = new Transaction();
                transaction.begin();
                try {
                    Thread.sleep(randGen.nextInt(1000) * 10);
                    log.info("Current ReadView is {}.", transaction.getReadView());
                } catch (InterruptedException e) {
                    log.warn("unexpected interrupt.", e);
                } finally {
                    transaction.commit();
                    latch.countDown();
                }
            });
            Thread.sleep(100);
        }

        latch.await();
        threadPool.shutdown();
    }
}
