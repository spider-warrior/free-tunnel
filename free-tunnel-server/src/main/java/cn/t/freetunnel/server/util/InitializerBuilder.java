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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class InitializerBuilder {

    public static NettyTcpChannelInitializer buildHttpProxyServerChannelInitializer() {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //idle handler
        daemonConfigBuilder.configIdleHandler(HttpProxyServerConfig.HTTP_PROXY_READ_TIME_OUT_IN_SECONDS, HttpProxyServerConfig.HTTP_PROXY_WRITE_TIME_OUT_IN_SECONDS, HttpProxyServerConfig.HTTP_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        List<Supplier<? extends ChannelHandler>> supplierList = new ArrayList<>();
        //http request decoder
        supplierList.add(HttpRequestDecoder::new);
        //http response encoder
        supplierList.add(HttpResponseEncoder::new);
        //http message aggregate
        supplierList.add(() -> new HttpObjectAggregator(1024 * 1024 * 4));
        //http proxy handler
        supplierList.add(HttpProxyServerMessageHandler::new);
        daemonConfigBuilder.configHandler(supplierList);
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer buildHttpFetchMessageClientChannelInitializer(ChannelHandlerContext remoteChannelHandlerContext, TunnelBuildResultListener tunnelBuildResultListener) {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //http encoder
        daemonConfigBuilder.configM2bEncoder(Collections.singletonList(ProxiedRequestEncoder::new));
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(() -> new FetchMessageHandler(remoteChannelHandlerContext, tunnelBuildResultListener)));
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer buildHttpsFetchMessageClientChannelInitializer(ChannelHandlerContext remoteChannelHandlerContext, TunnelBuildResultListener tunnelBuildResultListener) {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(() -> new FetchMessageHandler(remoteChannelHandlerContext, tunnelBuildResultListener)));
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer buildHttpFetchMessageClientChannelInitializerForSocks5(ChannelHandlerContext remoteChannelHandlerContext, TunnelBuildResultListener tunnelBuildResultListener) {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(() -> new Socks5TunnelServerFetchHandler(remoteChannelHandlerContext, tunnelBuildResultListener)));
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer buildHttpFetchMessageClientChannelInitializerForSocks5FreeTunnelClient(ChannelHandlerContext remoteChannelHandlerContext, TunnelBuildResultListener tunnelBuildResultListener) {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(() -> new Socks5TunnelServerFetchHandlerForFreeTunnelClient(remoteChannelHandlerContext, tunnelBuildResultListener)));
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer buildSocks5ProxyServerChannelInitializer() {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //idle
        daemonConfigBuilder.configIdleHandler(Socks5TunnelServerConfig.SOCKS5_PROXY_READ_TIME_OUT_IN_SECONDS, Socks5TunnelServerConfig.SOCKS5_PROXY_WRITE_TIME_OUT_IN_SECONDS, Socks5TunnelServerConfig.SOCKS5_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        daemonConfigBuilder.configHandler(Collections.singletonList(Socks5TunnelServerMessageHandler::new));
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }
}
