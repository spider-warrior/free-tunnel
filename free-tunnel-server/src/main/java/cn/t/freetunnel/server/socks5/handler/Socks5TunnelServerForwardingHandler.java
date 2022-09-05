package cn.t.freetunnel.server.socks5.handler;

import cn.t.freetunnel.common.handler.ForwardingMessageHandler;
import cn.t.freetunnel.common.util.TunnelUtil;
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
        boolean remoteOpen = remoteChannelHandlerContext.channel().isOpen();
        if(remoteOpen) {
            logger.info("[{} -> {}]: 客户端通道断开, 释放代理资源: [{} -> {}]", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannelHandlerContext.channel().localAddress(), remoteChannelHandlerContext.channel().remoteAddress());
            TunnelUtil.closeImmediately(remoteChannelHandlerContext);
        } else {
            logger.info("[{} -> {}]: 断开连接", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        }
    }

    public Socks5TunnelServerForwardingHandler(ChannelHandlerContext remoteChannelHandlerContext) {
        super(remoteChannelHandlerContext);
    }
}
