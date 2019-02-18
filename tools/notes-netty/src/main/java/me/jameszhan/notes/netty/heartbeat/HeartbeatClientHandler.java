package me.jameszhan.notes.netty.heartbeat;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * Greated by IntelliJ IDEA
 *
 * @author James Zhan
 *
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-02-18 03:06
 */
@ChannelHandler.Sharable
@Slf4j
public class HeartbeatClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("HeartbeatClientHandler({} -> {}) channelActive on {}", ctx.channel().localAddress(),
                ctx.channel().remoteAddress(), new Date());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("HeartbeatClientHandler({} -> {}) channelInactive on {}", ctx.channel().localAddress(),
                ctx.channel().remoteAddress(), new Date());
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = (String) msg;
        log.info("HeartbeatClientHandler({} -> {}) channelRead {}", ctx.channel().localAddress(),
                ctx.channel().remoteAddress(), msg);
        if (message.equals("Heartbeat")) {
            ctx.write("has read message from server");
            ctx.flush();
        }
        ReferenceCountUtil.release(msg);
    }

}
