package cn.t.freetunnel.server.tunnelprovider;

import cn.t.freetunnel.common.constants.NettyAttrConstants;
import cn.t.freetunnel.common.constants.TunnelConstants;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import cn.t.freetunnel.common.util.TunnelUtil;
import cn.t.freetunnel.server.util.InitializerBuilder;
import cn.t.tool.nettytool.daemon.client.NettyTcpClient;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

/**
 * UnPooledTunnelProvider
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-10 14:10
 **/
public class UnPooledTunnelProvider {
    public static void acquireTcpTunnelForHttp(ChannelHandlerContext localContext, String targetHost, int targetPort, TunnelBuildResultListener listener) {
        InetSocketAddress clientAddress = (InetSocketAddress)localContext.channel().remoteAddress();
        String clientName = TunnelUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        NettyTcpChannelInitializer channelInitializer = InitializerBuilder.buildHttpFetchMessageClientChannelInitializer(localContext, listener);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, targetHost, targetPort, channelInitializer, TunnelConstants.WORKER_GROUP, false, false);
        nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CONTEXT, localContext);
        nettyTcpClient.start();
    }
    public static void acquireTcpTunnelForHttps(ChannelHandlerContext localContext, String targetHost, int targetPort, TunnelBuildResultListener listener) {
        InetSocketAddress clientAddress = (InetSocketAddress)localContext.channel().remoteAddress();
        String clientName = TunnelUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        NettyTcpChannelInitializer channelInitializer = InitializerBuilder.buildHttpsFetchMessageClientChannelInitializer(localContext, listener);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, targetHost, targetPort, channelInitializer, TunnelConstants.WORKER_GROUP, false, false);
        nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CONTEXT, localContext);
        nettyTcpClient.start();
    }
    public static void acquireTcpTunnelForSocks5(ChannelHandlerContext localContext, String targetHost, int targetPort, TunnelBuildResultListener listener) {
        InetSocketAddress clientAddress = (InetSocketAddress)localContext.channel().remoteAddress();
        String clientName = TunnelUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        NettyTcpChannelInitializer channelInitializer = InitializerBuilder.buildHttpFetchMessageClientChannelInitializerForSocks5(localContext, listener);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, targetHost, targetPort, channelInitializer, TunnelConstants.WORKER_GROUP, false, false);
        nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CONTEXT, localContext);
        nettyTcpClient.start();
    }

    public static void acquireTcpTunnelForSocks5FreeTunnelClient(ChannelHandlerContext localContext, String targetHost, int targetPort, TunnelBuildResultListener listener) {
        InetSocketAddress clientAddress = (InetSocketAddress)localContext.channel().remoteAddress();
        String clientName = TunnelUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        NettyTcpChannelInitializer channelInitializer = InitializerBuilder.buildHttpFetchMessageClientChannelInitializerForSocks5FreeTunnelClient(localContext, listener);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, targetHost, targetPort, channelInitializer, TunnelConstants.WORKER_GROUP, false, false);
        nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CONTEXT, localContext);
        nettyTcpClient.start();
    }
}
