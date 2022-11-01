package cn.t.freetunnel.client.socks5.listener;

import cn.t.freetunnel.common.exception.TunnelException;
import cn.t.freetunnel.common.listener.TunnelReadyListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public abstract class Socks5TunnelClientReadyListener extends TunnelReadyListener {

    @Override
    protected void operationFailed(ChannelFuture future) {
        throw new TunnelException("未实现operationFailed: " + this.getClass());
    }

    public Socks5TunnelClientReadyListener(Channel localChannelHandlerContext, Channel remoteChannel, String host, int port) {
        super(localChannelHandlerContext, remoteChannel, host, port);
    }
}
