package me.jameszhan.notes.netty.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Greated by IntelliJ IDEA
 *
 * @author James Zhan
 *
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-02-18 03:01
 */
@Slf4j
@ChannelHandler.Sharable
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHandlerHolder {
    private static final int MAX_ATTEMPTS = 10;
    private final Bootstrap bootstrap;
    private final Timer timer;
    private final int port;

    private final String host;

    private volatile boolean reconnect = true;
    private int attempts;


    public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, int port,String host, boolean reconnect) {
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.port = port;
        this.host = host;
        this.reconnect = reconnect;
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
        System.out.println("链接关闭");
        log.info("当前链路({} -> {})链接关闭.", ctx.channel().localAddress(), ctx.channel().remoteAddress());
        if (reconnect) {
            if (attempts < MAX_ATTEMPTS) {
                attempts++;
                //重连的间隔时间会越来越长
                int timeout = 2 << attempts;
                timer.newTimeout(this, timeout, TimeUnit.MILLISECONDS);
            }
        }
        ctx.fireChannelInactive();
    }


    public void run(Timeout timeout) {
        ChannelFuture future;
        //bootstrap已经初始化好了，只需要将handler填入就可以了
        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override protected void initChannel(Channel ch) {
                    ch.pipeline().addLast(handlers());
                }
            });
            future = bootstrap.connect(host,port);
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
