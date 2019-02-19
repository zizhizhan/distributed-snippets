package me.jameszhan.notes.netty;

import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Greated by IntelliJ IDEA
 *
 * @author James Zhan
 *
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-02-19 21:09
 */
@Slf4j
public class HeartbeatTimerTest {

    private static final HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS);

    @BeforeClass
    public static void setUp() {
        timer.start();
    }

    @Test
    public void fixedDuration() {
        timer.newTimeout(timeout -> {
            timeout.timer().newTimeout(timeout.task(), 1, TimeUnit.SECONDS);
            log.info("fixedDuration executed at {}.", LocalDateTime.now());
        }, 1, TimeUnit.SECONDS);
    }

    @Test
    public void unfixedDuration() {
        AtomicInteger tryTimes = new AtomicInteger(0);
        timer.newTimeout(timeout -> {
            timeout.timer().newTimeout(timeout.task(), tryTimes.addAndGet(2), TimeUnit.SECONDS);
            log.info("unfixedDuration executed at {}.", LocalDateTime.now());
        }, tryTimes.get(), TimeUnit.SECONDS);
    }

    @Test
    public void unfixedDuration2() {
        AtomicInteger tryTimes = new AtomicInteger(0);
        timer.newTimeout(timeout -> {
            timeout.timer().newTimeout(timeout.task(), 2 << tryTimes.getAndIncrement(), TimeUnit.SECONDS);
            log.info("unfixedDuration executed at {}.", LocalDateTime.now());
        }, tryTimes.get(), TimeUnit.SECONDS);
    }

    @After
    public void waitTaskDone() throws Exception {
        Thread.sleep(20000);
    }

    @AfterClass
    public static void tearDown() {
        timer.stop();
    }


    public static void main1(String[] args) throws Exception {
        HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS);

        log.info("App Start at {}.", LocalDateTime.now());
        timer.newTimeout(timeout -> {
            Thread.sleep(3000);
            log.info("Task1 executed at {}.", LocalDateTime.now());
        }, 3, TimeUnit.SECONDS);

        timer.newTimeout(timeout -> log.info("Task2 executed at {}.", LocalDateTime.now()),
                10, TimeUnit.SECONDS);

        timer.newTimeout(timeout -> {
            timeout.timer().newTimeout(timeout.task(), 1, TimeUnit.SECONDS);
            log.info("LoopTask executed at {}.", LocalDateTime.now());
        }, 1, TimeUnit.SECONDS);

        int tryTimes = 1;
        timer.newTimeout(timeout -> {
            timeout.timer().newTimeout(timeout.task(), 1 << tryTimes, TimeUnit.SECONDS);
            log.info("FibTask executed at {}.", LocalDateTime.now());
        }, 1, TimeUnit.SECONDS);

        // timer.start();
        Thread.sleep(20000);
        timer.stop();
    }
}


