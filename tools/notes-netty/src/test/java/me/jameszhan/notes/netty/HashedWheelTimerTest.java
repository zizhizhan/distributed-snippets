package me.jameszhan.notes.netty;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Greated by IntelliJ IDEA
 *
 * @author James Zhan
 *
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-02-19 00:08
 */
@Slf4j
public class HashedWheelTimerTest {

    private static final HashedWheelTimer timer = new HashedWheelTimer();

    public static void main(String[] args) {
        timer.newTimeout(timeout -> {
            log.info("timeout is {}.", timeout);
        }, 10, TimeUnit.SECONDS);

        timer.newTimeout(t -> {
            new Thread(() -> {
                log.info("BEGIN");
                log.info("{}", timer);
                try {
                    TimeUnit.SECONDS.sleep(100);
                } catch (InterruptedException e) {
                    log.info("Unexpected Interrupt.", e);
                }
                log.info("FINISH");
            }).start();
        }, 1, TimeUnit.SECONDS);
    }


}
