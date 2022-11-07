package cn.t.freetunnel.client.socks5.tunnelprovider;

import cn.t.freetunnel.client.socks5.constants.ClientAttrConstants;
import cn.t.freetunnel.client.socks5.constants.Socks5TunnelClientConfig;
import cn.t.freetunnel.client.socks5.util.InitializerBuilder;
import cn.t.freetunnel.client.socks5.util.Socks5MessageUtil;
import cn.t.freetunnel.common.constants.FreeTunnelConstants;
import cn.t.freetunnel.common.constants.NettyAttrConstants;
import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.constants.TunnelConstants;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import cn.t.freetunnel.common.util.TunnelUtil;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.client.NettyTcpClient;
import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChannelProvider {

    private static final Logger logger = LoggerFactory.getLogger(FreeTunnelConstants.TUNNEL_EVENT_LOGGER_NAME);
    private static final AttributeKey<Long> channelUpTime = AttributeKey.newInstance("channelUpTime");

//    private final Queue<Channel> idledTunnelPool = new ConcurrentLinkedQueue<>();
//    private final Set<Channel> inUseTunnelPool = ConcurrentHashMap.newKeySet();

    private final Queue<Channel> idledTunnelPool = new LinkedList<>();
    private final Set<Channel> inUseTunnelPool = new HashSet<>();

    public void acquireSocks5Tunnel(Channel localChannel, String targetHost, int targetPort, TunnelBuildResultListener listener) {
        Channel channel;
        while ((channel = idledTunnelPool.poll()) != null && !channel.isOpen());
        if(channel != null && channel.isOpen()) {
            logger.info("复用连接,channel: {}, target host: {}, target port: {}", channel, targetHost, targetPort);
            inUseTunnelPool.add(channel);
            channel.attr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CHANNEL).set(localChannel);
            channel.attr(ClientAttrConstants.TUNNEL_IN_USE).set(Boolean.TRUE);
            TunnelSpecification tunnelSpecification = channel.attr(ClientAttrConstants.TUNNEL_SPECIFICATION).get();
            tunnelSpecification.setTargetHost(targetHost);
            tunnelSpecification.setTargetPort(targetPort);
            tunnelSpecification.setTunnelBuildResultListener(listener);
            ByteBuf outputBuf = Socks5MessageUtil.buildConnectBuf(channel.alloc(), targetHost, targetPort);
            channel.writeAndFlush(outputBuf);
            logger.info("cmd request, channel: {}", channel);
        } else {
            logger.info("请求建立连接, localChannel: {}, target host: {}, target port: {}", localChannel, targetHost, targetPort);
            InetSocketAddress clientAddress = (InetSocketAddress)localChannel.remoteAddress();
            String clientName = TunnelUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
            NettyTcpChannelInitializer channelInitializer = InitializerBuilder.buildHttpProxyServerViaSocks5ClientChannelInitializer();
            NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, Socks5TunnelClientConfig.socks5ServerHost, Socks5TunnelClientConfig.socks5ServerPort, channelInitializer, TunnelConstants.WORKER_GROUP, false, false);
            nettyTcpClient.childAttr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CHANNEL, localChannel);
            nettyTcpClient.childAttr(ClientAttrConstants.TUNNEL_IN_USE, Boolean.TRUE);
            nettyTcpClient.childAttr(ClientAttrConstants.TUNNEL_SPECIFICATION, new TunnelSpecification(targetHost, targetPort, listener));
            nettyTcpClient.addListener(new ClientLifeCycleListener());
            nettyTcpClient.start();
        }
    }

    public ChannelProvider(EventLoop eventLoop) {
        eventLoop.scheduleAtFixedRate(
            () -> {
                logger.info("连接池统计, 使用中: {}, 空闲: {}", inUseTunnelPool.size(), idledTunnelPool.size());
                long now = System.currentTimeMillis();
                Iterator<Channel> iterator = idledTunnelPool.iterator();
                while (iterator.hasNext()) {
                    Channel channel = iterator.next();
                    if(channel.isOpen()) {
                        Attribute<Long> attr = channel.attr(channelUpTime);
                        if(now - attr.get() > 10000) {
                            channel.writeAndFlush(TunnelCommand.HEART_BEAT).addListener(future -> {
                                if(future.isSuccess()) {
                                    attr.set(now);
                                }
                            });
                        }
                    }
                }
            },
            5,
            10,
            TimeUnit.SECONDS
        );
    }

    public void closeTunnel(Channel remoteChannel) {
        if(remoteChannel.isOpen()) {
            inUseTunnelPool.remove(remoteChannel);
            idledTunnelPool.add(remoteChannel);
            logger.info("返还连接,channel: {}, 使用中: {}, 可复用: {}", remoteChannel, inUseTunnelPool.size(), idledTunnelPool.size());
            remoteChannel.attr(channelUpTime).set(System.currentTimeMillis());
        } else {
            logger.error("返还连接不可用,channel: {}", remoteChannel);
        }
    }

    private class ClientLifeCycleListener implements DaemonListener {

        @Override
        public void startup(DaemonService server, Channel channel) {
            TunnelSpecification tunnelSpecification = channel.attr(ClientAttrConstants.TUNNEL_SPECIFICATION).get();
            logger.info("新建通道: {}, targetHost: {}, targetPort: {}", channel, tunnelSpecification.getTargetHost(), tunnelSpecification.getTargetPort());
            inUseTunnelPool.add(channel);
        }

        @Override
        public void close(DaemonService server, Channel channel) {
            idledTunnelPool.remove(channel);
            inUseTunnelPool.remove(channel);
        }

        @Override
        public void close(DaemonService server, Channel channel, Throwable t) {
            idledTunnelPool.remove(channel);
            inUseTunnelPool.remove(channel);
        }
    }
}
