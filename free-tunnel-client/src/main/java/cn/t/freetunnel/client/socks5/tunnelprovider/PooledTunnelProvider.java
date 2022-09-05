package cn.t.freetunnel.client.socks5.tunnelprovider;

import cn.t.freetunnel.client.socks5.util.InitializerBuilder;
import cn.t.freetunnel.client.socks5.util.Socks5MessageUtil;
import cn.t.freetunnel.common.constants.FreeTunnelConstants;
import cn.t.freetunnel.common.constants.NettyAttrConstants;
import cn.t.freetunnel.common.constants.Socks5TunnelClientConfig;
import cn.t.freetunnel.common.constants.TunnelConstants;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import cn.t.freetunnel.common.util.TunnelUtil;
import cn.t.tool.nettytool.daemon.client.NettyTcpClient;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * PooledTunnelProvider
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-10 14:10
 **/
public class PooledTunnelProvider {

    private static final Logger logger = LoggerFactory.getLogger(FreeTunnelConstants.TUNNEL_EVENT_LOGGER_NAME);

    private static final Queue<ChannelHandlerContext> tunnelPool = new ConcurrentLinkedQueue<>();

    public static void acquireSocks5Tunnel(ChannelHandlerContext localContext, String targetHost, int targetPort, Socks5TunnelClientConfig socks5TunnelClientConfig, TunnelBuildResultListener listener) {
        ChannelHandlerContext context;
        while ((context = tunnelPool.poll()) != null && !context.channel().isOpen());
        if(context != null && context.channel().isOpen()) {
            logger.info("复用连接,channel: {}, target host: {}, target port: {}", context.channel(), targetHost, targetPort);
            context.channel().attr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CONTEXT).set(localContext);
            context.channel().attr(NettyAttrConstants.CONNECT_TUNNEL_BUILD_RESULT_LISTENER).set(listener);
            ByteBuf outputBuf = Socks5MessageUtil.buildConnectBuf(context.alloc(), targetHost, targetPort);
            context.channel().writeAndFlush(outputBuf);
        } else {
            logger.info("请求建立连接, localChannel: {}, target host: {}, target port: {}", localContext.channel(), targetHost, targetPort);
            InetSocketAddress clientAddress = (InetSocketAddress)localContext.channel().remoteAddress();
            String clientName = TunnelUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
            NettyTcpChannelInitializer channelInitializer = InitializerBuilder.buildHttpProxyServerViaSocks5ClientChannelInitializer();
            NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, socks5TunnelClientConfig.getSocks5ServerHost(), socks5TunnelClientConfig.getSocks5ServerPort(), channelInitializer, TunnelConstants.WORKER_GROUP, false, false);
            nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_USERNAME, socks5TunnelClientConfig.getUsername());
            nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_PASSWORD, socks5TunnelClientConfig.getPassword());
            nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_SECURITY, socks5TunnelClientConfig.getSecurity());
            nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TARGET_HOST, targetHost);
            nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TARGET_PORT, targetPort);
            nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CONTEXT, localContext);
            nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_BUILD_RESULT_LISTENER, listener);
            nettyTcpClient.start();
        }
    }

    public static void closeTunnel(ChannelHandlerContext remoteChannelHandlerContext) {
        if(remoteChannelHandlerContext.channel().isOpen()) {
            tunnelPool.add(remoteChannelHandlerContext);
            logger.info("返还连接,channel: {},可复用连接数量: {}", remoteChannelHandlerContext.channel(), tunnelPool.size());
        } else {
            logger.error("返还连接不可用,channel: {}", remoteChannelHandlerContext.channel());
        }
    }
}
