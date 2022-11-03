package cn.t.freetunnel.client.socks5.util;

import cn.t.freetunnel.client.socks5.constants.HttpSocks5TunnelClientConfig;
import cn.t.freetunnel.client.socks5.handler.HttpSocks5TunnelClientHandler;
import cn.t.freetunnel.client.socks5.handler.Socks5TunnelClientMessageHandler;
import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class InitializerBuilder {

    public static NettyTcpChannelInitializer buildHttpProxyServerViaSocks5ClientChannelInitializer() {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        daemonConfigBuilder.configHandler(Collections.singletonList(Socks5TunnelClientMessageHandler::new));
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer httpSocks5TunnelClientInitializer() {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //idle
        daemonConfigBuilder.configIdleHandler(HttpSocks5TunnelClientConfig.HTTP_PROXY_READ_TIME_OUT_IN_SECONDS, HttpSocks5TunnelClientConfig.HTTP_PROXY_WRITE_TIME_OUT_IN_SECONDS, HttpSocks5TunnelClientConfig.HTTP_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        List<Supplier<? extends ChannelHandler>> supplierList = new ArrayList<>();
        //http response encoder
        supplierList.add(HttpResponseEncoder::new);
        //http request decoder
        supplierList.add(HttpRequestDecoder::new);
        supplierList.add(() -> new HttpObjectAggregator(1024 * 1024 * 2));
        supplierList.add(() -> new HttpSocks5TunnelClientHandler());
        daemonConfigBuilder.configHandler(supplierList);
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }
}
