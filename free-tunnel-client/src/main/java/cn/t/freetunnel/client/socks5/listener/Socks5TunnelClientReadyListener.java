package cn.t.freetunnel.client.socks5.listener;

import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.listener.TunnelReadyListener;
import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Socks5TunnelClientReadyListener extends TunnelReadyListener {

    private static final Logger logger = LoggerFactory.getLogger(Socks5TunnelClientReadyListener.class);

    protected abstract void notifySuccess(ChannelFuture future);

    @Override
    protected void notifyFailed(Channel channel, ChannelFuture future) {
        if(remoteChannel.isOpen()) {
            logger.error("{}[{}]: 代理结果通知失败: {}:{}, 复位远程连接, 失败原因: {}", TunnelUtil.buildProxyTunnelName(localChannel, remoteChannel), this.getClass().getSimpleName(), host, port, future.cause());
            remoteChannel.writeAndFlush(TunnelCommand.RESET_STATUS_TO_COMMAND_REQUEST);
        }
    }

    public Socks5TunnelClientReadyListener(Channel localChannelHandlerContext, Channel remoteChannel, String host, int port) {
        super(localChannelHandlerContext, remoteChannel, host, port);
    }
}
