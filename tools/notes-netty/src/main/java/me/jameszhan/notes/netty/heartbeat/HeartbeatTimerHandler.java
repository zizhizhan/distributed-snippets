package me.jameszhan.notes.netty.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Greated by IntelliJ IDEA
 *
 * @author James Zhan
 *
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-02-20 00:51
 */
@Slf4j
@ChannelHandler.Sharable
public class HeartbeatTimerHandler extends ChannelInboundHandlerAdapter {
    private static final int MAX_ATTEMPTS = 16;
    private final HashedWheelTimer timer = new HashedWheelTimer();
    private final Bootstrap bootstrap;
    private final String host;
    private final int port;
    private int attempts = 0;

    public HeartbeatTimerHandler(Bootstrap bootstrap, String host, int port) {
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
    }

    /**
     * channel 链路每次 active 的时候，将其连接的次数重新置为 0
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        attempts = 0;
        log.info("当前链路({} -> {})已经激活了，重连尝试次数重新置为 0", ctx.channel().localAddress(),
                ctx.channel().remoteAddress());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("当前链路({} -> {})链接关闭.", ctx.channel().localAddress(), ctx.channel().remoteAddress());
        if (attempts < MAX_ATTEMPTS) {
            long delay = 2 << attempts;
            timer.newTimeout(this::reconnect, delay, TimeUnit.SECONDS);
            log.info("Reconnect after {}s.", delay);

            // 重连的间隔时间会越来越长
            attempts++;
        }
        ctx.fireChannelInactive();
    }

    private void reconnect(Timeout timeout) {
        ChannelFuture future;
        //bootstrap已经初始化好了，只需要将handler填入就可以了
        synchronized (bootstrap) {
            future = bootstrap.connect(host, port);
        }

        //future对象
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) {
                boolean succeed = f.isSuccess();
                if (!succeed) {
                    log.info("({} -> {}) 重连失败", f.channel().localAddress(), f.channel().remoteAddress());
                    f.channel().pipeline().fireChannelInactive();
                } else {
                    log.info("({} -> {}) 重连成功", f.channel().localAddress(), f.channel().remoteAddress());
                }
            }
        });
    }
}
