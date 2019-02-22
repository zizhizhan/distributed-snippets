package me.jameszhan.notes.netty.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Greated by IntelliJ IDEA
 *
 * @author James Zhan
 *
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-02-18 02:52
 */
public class HeartbeatClient {

    private final String host;
    private final int port;
    private final Bootstrap bootstrap;
    private final ConnectorIdleStateTrigger idleStateTrigger;
    private final HeartbeatTimerHandler heartbeatTimerHandler;

    public HeartbeatClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.bootstrap = new Bootstrap();
        this.idleStateTrigger = new ConnectorIdleStateTrigger();
        this.heartbeatTimerHandler = new HeartbeatTimerHandler(bootstrap, host, port);
    }

    public void connect() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO));

        try {
            ChannelFuture future;
            //进行连接
            synchronized (bootstrap) {
                bootstrap.handler(new ChannelInitializer<Channel>() {
                    @Override protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(
                                heartbeatTimerHandler,
                                new IdleStateHandler(0, 8, 0, TimeUnit.SECONDS),
                                idleStateTrigger,
                                new StringDecoder(),
                                new StringEncoder(),
                                new HeartbeatClientHandler());
                    }
                });

                future = bootstrap.connect(host, port);
            }

            // 以下代码在synchronized同步块外面是安全的
            future.sync();
        } catch (Throwable t) {
            throw new Exception("connects to  fails", t);
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 6666;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        new HeartbeatClient("127.0.0.1", port).connect();
    }


}
