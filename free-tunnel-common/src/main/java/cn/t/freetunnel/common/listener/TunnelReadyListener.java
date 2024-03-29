package cn.t.freetunnel.common.listener;

import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TunnelReadyListener implements ChannelFutureListener {

    private static final Logger logger = LoggerFactory.getLogger(TunnelReadyListener.class);

    protected final Channel remoteChannel;
    protected final String host;
    protected final int port;

    @Override
    public void operationComplete(ChannelFuture future) {
        if(future.isSuccess() && remoteChannel.isOpen()) {
            logger.info("[{}]: 通道消息发送成功", TunnelUtil.buildProxyTunnelName(future.channel(), remoteChannel));
            operationSuccess(future);
        } else {
            logger.error("[{}]: 通道消息发送失败, 地址: {}:{}, 本地连接状态: {}, 远端连接状态: {}, 失败原因: {}", TunnelUtil.buildProxyTunnelName(future.channel(), remoteChannel), host, port, future.channel().isOpen(), remoteChannel.isOpen(), future.cause());
            operationFailed(future);
        }
    }

    protected abstract void operationSuccess(ChannelFuture future);

    protected void operationFailed(ChannelFuture future) {
        logger.error("[{}]: 即将关闭连接: {}", TunnelUtil.buildProxyTunnelName(future.channel(), remoteChannel), remoteChannel);
        TunnelUtil.closeImmediately(remoteChannel);
    }

    public TunnelReadyListener(Channel remoteChannel, String host, int port) {
        this.remoteChannel = remoteChannel;
        this.host = host;
        this.port = port;
    }
}
