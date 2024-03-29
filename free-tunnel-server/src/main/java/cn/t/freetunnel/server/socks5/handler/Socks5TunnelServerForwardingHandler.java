package cn.t.freetunnel.server.socks5.handler;

import cn.t.freetunnel.common.handler.ForwardingMessageHandler;
import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 转发消息处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 23:46
 **/
@ChannelHandler.Sharable
public class Socks5TunnelServerForwardingHandler extends ForwardingMessageHandler {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if(remoteChannel.isOpen()) {
            logger.info("[{} -> {}]: 客户端通道断开, 释放代理资源: [{} -> {}]", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannel.localAddress(), remoteChannel.remoteAddress());
            TunnelUtil.closeImmediately(remoteChannel);
        } else {
            logger.info("[{} -> {}]: 断开连接", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        }
    }

    public Socks5TunnelServerForwardingHandler(Channel remoteChannel) {
        super(remoteChannel);
    }
}
