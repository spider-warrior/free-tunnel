package cn.t.freetunnel.server.socks5.listener;

import cn.t.freetunnel.common.constants.NettyHandlerName;
import cn.t.freetunnel.common.exception.TunnelException;
import cn.t.freetunnel.common.handler.EncryptMessageDecoder;
import cn.t.freetunnel.common.handler.EncryptMessageEncoder;
import cn.t.freetunnel.common.handler.LayerMessageDecoder;
import cn.t.freetunnel.common.handler.LayerMessageEncoder;
import cn.t.freetunnel.common.listener.TunnelReadyListener;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerCommandHandler;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerForwardingHandler;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerMessageHandler;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;

public class Socks5TunnelServerFirstTimeReadyListenerForFreeTunnelClient extends TunnelReadyListener {

    private final byte[] security;
    @Override
    protected void operationSuccess(ChannelFuture future) {
        ChannelPipeline channelPipeline = localChannel.pipeline();
        //加解密
        try {
            NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.ENCRYPT_MESSAGE_DECODER, new EncryptMessageDecoder(security));
            NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.ENCRYPT_MESSAGE_ENCODER, new EncryptMessageEncoder(security));
        } catch (Exception e) {
            throw new TunnelException(e);
        }
        //layer
        NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.LAYER_MESSAGE_DECODER, new LayerMessageDecoder());
        NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.LAYER_MESSAGE_ENCODER, new LayerMessageEncoder());
        //forwarding
        NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.SOCKS5_TUNNEL_SERVER_FORWARDING_MESSAGE_HANDLER, new Socks5TunnelServerForwardingHandler(remoteChannel));
        //command
        NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.SOCKS5_TUNNEL_SERVER_COMMAND_HANDLER, new Socks5TunnelServerCommandHandler(remoteChannel));
        //备份Socks5ProxyServerMessageHandler
        Socks5TunnelServerMessageHandler socks5TunnelServerMessageHandler = channelPipeline.remove(Socks5TunnelServerMessageHandler.class);
        NettyComponentUtil.addLastHandler(channelPipeline, "socks5ProxyServerMessageHandler", socks5TunnelServerMessageHandler);
    }

    public Socks5TunnelServerFirstTimeReadyListenerForFreeTunnelClient(Channel localChannel, Channel remoteChannel, String host, int port, byte[] security) {
        super(localChannel, remoteChannel, host, port);
        this.security = security;
    }
}
