package cn.t.freetunnel.server.socks5.listener;

import cn.t.freetunnel.common.constants.NettyHandlerName;
import cn.t.freetunnel.common.listener.TunnelReadyListener;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerMessageHandler;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerForwardingHandler;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

public class Socks5TunnelServerFirstTimeReadyListener extends TunnelReadyListener {

    @Override
    protected void notifySuccess(ChannelFuture future) {
        ChannelPipeline channelPipeline = localChannel.pipeline();
        //forwarding
        NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.SOCKS5_TUNNEL_SERVER_FORWARDING_MESSAGE_HANDLER, new Socks5TunnelServerForwardingHandler(remoteChannel));
        //备份Socks5ProxyServerMessageHandler
        Socks5TunnelServerMessageHandler socks5TunnelServerMessageHandler = channelPipeline.remove(Socks5TunnelServerMessageHandler.class);
        NettyComponentUtil.addLastHandler(channelPipeline, "socks5ProxyServerMessageHandler", socks5TunnelServerMessageHandler);
    }

    public Socks5TunnelServerFirstTimeReadyListener(Channel localChannel, Channel remoteChannel, String host, int port) {
        super(localChannel, remoteChannel, host, port);
    }
}
