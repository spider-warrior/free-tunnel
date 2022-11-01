package cn.t.freetunnel.client.socks5.tunnelprovider;

import cn.t.freetunnel.common.constants.Socks5TunnelClientConfig;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import io.netty.channel.Channel;
import io.netty.util.concurrent.FastThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StaticChannelProvider
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-10 14:10
 **/
public class StaticChannelProvider {

    private static final Logger logger = LoggerFactory.getLogger(StaticChannelProvider.class);
    private static final FastThreadLocal<ChannelProvider> threadChannelProvider = new FastThreadLocal<>();

    public static void acquireSocks5Tunnel(Channel localChannel, String targetHost, int targetPort, Socks5TunnelClientConfig socks5TunnelClientConfig, TunnelBuildResultListener listener) {
        ChannelProvider channelProvider = threadChannelProvider.get();
        if(channelProvider == null) {
            channelProvider = new ChannelProvider(localChannel.eventLoop());
            threadChannelProvider.set(channelProvider);
        }
        channelProvider.acquireSocks5Tunnel(localChannel, targetHost, targetPort, socks5TunnelClientConfig, listener);
    }

    public static void closeTunnel(Channel remoteChannel) {
        ChannelProvider channelProvider = threadChannelProvider.get();
        if(channelProvider != null) {
            channelProvider.closeTunnel(remoteChannel);
        } else {
            logger.error("连接异常, 当前连接关联的线程没有关联任何ChannelProvider, channel: {}, eventLoop: {}", remoteChannel, remoteChannel.eventLoop());
        }
    }
}
