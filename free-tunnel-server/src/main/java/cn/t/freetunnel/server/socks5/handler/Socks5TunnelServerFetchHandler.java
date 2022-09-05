package cn.t.freetunnel.server.socks5.handler;

import cn.t.freetunnel.common.constants.NettyAttrConstants;
import cn.t.freetunnel.common.handler.FetchMessageHandler;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Socks5TunnelServerFetchHandler extends FetchMessageHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if(ctx.channel().hasAttr(NettyAttrConstants.CLOSE_BY_CALL_METHOD)) {
            log.info("[{} -> {}]: 断开连接", ctx.channel().localAddress(), ctx.channel().remoteAddress());
        } else {
            if(remoteChannelHandlerContext.channel().isOpen()) {
                closeRemote(ctx);
            }
        }
    }

    protected void closeRemote(ChannelHandlerContext ctx) {
        log.info("[{} -> {}]: 断开连接, 关闭通道连接: [{} -> {}]", ctx.channel().localAddress(), ctx.channel().remoteAddress(), remoteChannelHandlerContext.channel().remoteAddress(), remoteChannelHandlerContext.channel().localAddress());
        remoteChannelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public Socks5TunnelServerFetchHandler(ChannelHandlerContext remoteChannelHandlerContext, TunnelBuildResultListener tunnelBuildResultListener) {
        super(remoteChannelHandlerContext, tunnelBuildResultListener);
    }
}
