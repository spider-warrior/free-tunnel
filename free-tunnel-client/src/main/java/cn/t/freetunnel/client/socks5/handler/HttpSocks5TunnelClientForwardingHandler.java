package cn.t.freetunnel.client.socks5.handler;

import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.handler.ForwardingMessageHandler;
import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息转发处理器(左 -> 右)
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 23:46
 **/
public class HttpSocks5TunnelClientForwardingHandler extends ForwardingMessageHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if(TunnelUtil.isClosedByCallMethod(ctx)) {
            log.info("[{} -> {}]: 断开连接", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        } else {
            if(remoteChannelHandlerContext.channel().isOpen()) {
                log.info("[{} -> {}]: 断开连接, 复位通道: [{} -> {}]", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannelHandlerContext.channel().localAddress(), remoteChannelHandlerContext.channel().remoteAddress());
                remoteChannelHandlerContext.channel().writeAndFlush(TunnelCommand.RESET_STATUS_TO_COMMAND_REQUEST);
            }
        }
    }

    public HttpSocks5TunnelClientForwardingHandler(ChannelHandlerContext remoteChannelHandlerContext) {
        super(remoteChannelHandlerContext);
    }
}
