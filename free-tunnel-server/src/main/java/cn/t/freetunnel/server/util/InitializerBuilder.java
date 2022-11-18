package cn.t.freetunnel.server.util;

import cn.t.freetunnel.common.handler.FetchMessageHandler;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import cn.t.freetunnel.server.constants.HttpProxyServerConfig;
import cn.t.freetunnel.server.constants.Socks5TunnelServerConfig;
import cn.t.freetunnel.server.http.encoder.ProxiedRequestEncoder;
import cn.t.freetunnel.server.http.handler.HttpProxyServerMessageHandler;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerFetchHandler;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerFetchHandlerForFreeTunnelClient;
import cn.t.freetunnel.server.socks5.handler.Socks5TunnelServerMessageHandler;
import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class InitializerBuilder {

    public static NettyTcpChannelInitializer buildHttpProxyServerChannelInitializer() {
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //idle handler
        daemonConfigBuilder.configIdleHandler(HttpProxyServerConfig.HTTP_PROXY_READ_TIME_OUT_IN_SECONDS, HttpProxyServerConfig.HTTP_PROXY_WRITE_TIME_OUT_IN_SECONDS, HttpProxyServerConfig.HTTP_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        List<Function<SocketChannel, ? extends ChannelHandler>> factoryList = new ArrayList<>();
        //http request decoder
        factoryList.add(ch -> new HttpRequestDecoder());
        //http response encoder
        factoryList.add(ch -> new HttpResponseEncoder());
        //http message aggregate
        factoryList.add(ch -> new HttpObjectAggregator(1024 * 1024 * 4));
        //http proxy handler
        factoryList.add(ch -> new HttpProxyServerMessageHandler());
        daemonConfigBuilder.configHandler(factoryList);
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer buildHttpFetchMessageClientChannelInitializer(Channel remoteChannel, TunnelBuildResultListener tunnelBuildResultListener) {
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //http encoder
        daemonConfigBuilder.configM2bEncoder(Collections.singletonList(ch -> new ProxiedRequestEncoder()));
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(ch -> new FetchMessageHandler(remoteChannel, tunnelBuildResultListener)));
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer buildHttpsFetchMessageClientChannelInitializer(Channel remoteChannel, TunnelBuildResultListener tunnelBuildResultListener) {
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(ch -> new FetchMessageHandler(remoteChannel, tunnelBuildResultListener)));
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer buildHttpFetchMessageClientChannelInitializerForSocks5(Channel remoteChannel, TunnelBuildResultListener tunnelBuildResultListener) {
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(ch -> new Socks5TunnelServerFetchHandler(remoteChannel, tunnelBuildResultListener)));
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer buildHttpFetchMessageClientChannelInitializerForSocks5FreeTunnelClient(Channel remoteChannel, TunnelBuildResultListener tunnelBuildResultListener) {
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(ch -> new Socks5TunnelServerFetchHandlerForFreeTunnelClient(remoteChannel, tunnelBuildResultListener)));
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer buildSocks5ProxyServerChannelInitializer() {
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //idle
        daemonConfigBuilder.configIdleHandler(Socks5TunnelServerConfig.SOCKS5_PROXY_READ_TIME_OUT_IN_SECONDS, Socks5TunnelServerConfig.SOCKS5_PROXY_WRITE_TIME_OUT_IN_SECONDS, Socks5TunnelServerConfig.SOCKS5_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        daemonConfigBuilder.configHandler(Collections.singletonList(ch -> new Socks5TunnelServerMessageHandler()));
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }
}
