package cn.t.freetunnel.client.socks5.handler;

import cn.t.freetunnel.client.socks5.util.Socks5MessageUtil;
import cn.t.freetunnel.common.handler.ForwardingMessageHandler;
import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 终端输入消息转发处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 23:46
 **/
public class HttpSocks5TunnelClientForwardingHandler extends ForwardingMessageHandler {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if(TunnelUtil.isClosedByCallMethod(ctx)) {
            logger.info("[{} -> {}]: 断开连接", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        } else {
            if(remoteChannel.isOpen()) {
                if(!remoteChannel.config().isAutoRead()) {
                    remoteChannel.config().setAutoRead(true);
                }
                logger.info("[{} -> {}]: 断开连接, 复位通道: [{} -> {}]", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannel.localAddress(), remoteChannel.remoteAddress());
                Socks5MessageUtil.sendResetChannelRequest(remoteChannel);
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(!TunnelUtil.isClosedByCallMethod(ctx)) {
            super.channelRead(ctx, msg);
        } else {
            logger.warn("local channel has been closed, ignore forwarding message: {}", msg);
        }
    }

    public HttpSocks5TunnelClientForwardingHandler(Channel remoteChannel) {
        super(remoteChannel);
    }
}
