package cn.t.freetunnel.client.socks5.listener;

import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.listener.TunnelReadyListener;
import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Socks5TunnelClientReadyListener extends TunnelReadyListener {

    private static final Logger logger = LoggerFactory.getLogger(Socks5TunnelClientReadyListener.class);

    protected abstract void notifySuccess(ChannelFuture future);

    @Override
    protected void notifyFailed(ChannelHandlerContext ctx, ChannelFuture future) {
        if(remoteChannelHandlerContext.channel().isOpen()) {
            logger.error("{}[{}]: 代理结果通知失败: {}:{}, 复位远程连接, 失败原因: {}", TunnelUtil.buildProxyTunnelName(localChannelHandlerContext, remoteChannelHandlerContext), this.getClass().getSimpleName(), host, port, future.cause());
            remoteChannelHandlerContext.channel().writeAndFlush(TunnelCommand.RESET_STATUS_TO_COMMAND_REQUEST);
        }
    }

    public Socks5TunnelClientReadyListener(ChannelHandlerContext localChannelHandlerContext, ChannelHandlerContext remoteChannelHandlerContext, String host, int port) {
        super(localChannelHandlerContext, remoteChannelHandlerContext, host, port);
    }
}
