package cn.t.freetunnel.server.tunnelprovider;

import cn.t.freetunnel.common.constants.NettyAttrConstants;
import cn.t.freetunnel.common.constants.TunnelConstants;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import cn.t.freetunnel.common.util.TunnelUtil;
import cn.t.freetunnel.server.util.InitializerBuilder;
import cn.t.tool.nettytool.daemon.client.NettyTcpClient;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * UnPooledTunnelProvider
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-10 14:10
 **/
public class UnPooledTunnelProvider {
    public static void acquireTcpTunnelForHttp(Channel localChannel, String targetHost, int targetPort, TunnelBuildResultListener listener) {
        InetSocketAddress clientAddress = (InetSocketAddress)localChannel.remoteAddress();
        String clientName = TunnelUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        NettyTcpChannelInitializer channelInitializer = InitializerBuilder.buildHttpFetchMessageClientChannelInitializer(localChannel, listener);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, targetHost, targetPort, channelInitializer, TunnelConstants.WORKER_GROUP, false, false);
        nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CHANNEL, localChannel);
        nettyTcpClient.childAttr(NettyAttrConstants.EVENT_EXECUTOR, localChannel.eventLoop());
        nettyTcpClient.start();
    }
    public static void acquireTcpTunnelForHttps(Channel localChannel, String targetHost, int targetPort, TunnelBuildResultListener listener) {
        InetSocketAddress clientAddress = (InetSocketAddress)localChannel.remoteAddress();
        String clientName = TunnelUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        NettyTcpChannelInitializer channelInitializer = InitializerBuilder.buildHttpsFetchMessageClientChannelInitializer(localChannel, listener);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, targetHost, targetPort, channelInitializer, TunnelConstants.WORKER_GROUP, false, false);
        nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CHANNEL, localChannel);
        nettyTcpClient.childAttr(NettyAttrConstants.EVENT_EXECUTOR, localChannel.eventLoop());
        nettyTcpClient.start();
    }
    public static void acquireTcpTunnelForSocks5(Channel localChannel, String targetHost, int targetPort, TunnelBuildResultListener listener) {
        InetSocketAddress clientAddress = (InetSocketAddress)localChannel.remoteAddress();
        String clientName = TunnelUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        NettyTcpChannelInitializer channelInitializer = InitializerBuilder.buildHttpFetchMessageClientChannelInitializerForSocks5(localChannel, listener);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, targetHost, targetPort, channelInitializer, TunnelConstants.WORKER_GROUP, false, false);
        nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CHANNEL, localChannel);
        nettyTcpClient.childAttr(NettyAttrConstants.EVENT_EXECUTOR, localChannel.eventLoop());
        nettyTcpClient.start();
    }

    public static void acquireTcpTunnelForSocks5FreeTunnelClient(Channel localChannel, String targetHost, int targetPort, TunnelBuildResultListener listener) {
        InetSocketAddress clientAddress = (InetSocketAddress)localChannel.remoteAddress();
        String clientName = TunnelUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        NettyTcpChannelInitializer channelInitializer = InitializerBuilder.buildHttpFetchMessageClientChannelInitializerForSocks5FreeTunnelClient(localChannel, listener);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, targetHost, targetPort, channelInitializer, TunnelConstants.WORKER_GROUP, false, false);
        nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CHANNEL, localChannel);
        nettyTcpClient.childAttr(NettyAttrConstants.EVENT_EXECUTOR, localChannel.eventLoop());
        nettyTcpClient.start();
    }
}
