package cn.t.freetunnel.server.socks5.listener;

import cn.t.freetunnel.common.constants.NettyHandlerName;
import cn.t.freetunnel.common.listener.TunnelReadyListener;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerCommandHandler;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerForwardingHandler;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerMessageHandler;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;

public class Socks5TunnelServerReuseReadyListenerForFreeTunnelClient extends TunnelReadyListener {

    @Override
    protected void operationSuccess(ChannelFuture future) {
        ChannelPipeline channelPipeline = localChannel.pipeline();
        //forwardingHandler切换remoteContext
        Socks5TunnelServerForwardingHandler forwardingHandler = (Socks5TunnelServerForwardingHandler)channelPipeline.get(NettyHandlerName.SOCKS5_TUNNEL_SERVER_FORWARDING_MESSAGE_HANDLER);
        forwardingHandler.setRemoteChannel(remoteChannel);
        //commandHandler切换remoteContext
        Socks5TunnelServerCommandHandler commandHandler = (Socks5TunnelServerCommandHandler)channelPipeline.get(NettyHandlerName.SOCKS5_TUNNEL_SERVER_COMMAND_HANDLER);
        commandHandler.setRemoteChannel(remoteChannel);
        //备份Socks5ProxyServerMessageHandler
        Socks5TunnelServerMessageHandler socks5TunnelServerMessageHandler = channelPipeline.remove(Socks5TunnelServerMessageHandler.class);
        NettyComponentUtil.addLastHandler(channelPipeline, "socks5ProxyServerMessageHandler", socks5TunnelServerMessageHandler);
    }

    public Socks5TunnelServerReuseReadyListenerForFreeTunnelClient(Channel localChannel, Channel remoteChannel, String host, int port) {
        super(localChannel, remoteChannel, host, port);
    }
}
