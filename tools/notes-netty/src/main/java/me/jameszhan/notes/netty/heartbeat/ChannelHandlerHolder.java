package me.jameszhan.notes.netty.heartbeat;

import io.netty.channel.ChannelHandler;

/**
 * Greated by IntelliJ IDEA
 *
 * @author James Zhan
 *
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-02-18 03:03
 */
public interface ChannelHandlerHolder {

    ChannelHandler[] handlers();

}


