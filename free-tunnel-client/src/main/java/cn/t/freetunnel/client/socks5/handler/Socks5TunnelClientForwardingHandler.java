package cn.t.freetunnel.client.socks5.handler;

import cn.t.freetunnel.common.handler.ForwardingMessageHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息转发处理器(右 -> 左)
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 23:46
 **/
public class Socks5TunnelClientForwardingHandler extends ForwardingMessageHandler {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if(remoteChannel.isOpen()) {
            logger.info("[{} -> {}]: 服务端通道断开, 关闭客户端连接: [{} -> {}]", ctx.channel().localAddress(), ctx.channel().remoteAddress(), remoteChannel.remoteAddress(), remoteChannel.localAddress());
            remoteChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        } else {
            logger.info("[{} -> {}]: 断开连接", ctx.channel().localAddress(), ctx.channel().remoteAddress());
        }
    }

    public Socks5TunnelClientForwardingHandler(Channel remoteChannel) {
        super(remoteChannel);
    }
}
