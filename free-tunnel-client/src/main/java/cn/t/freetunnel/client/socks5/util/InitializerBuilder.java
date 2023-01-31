package cn.t.freetunnel.client.socks5.util;

import cn.t.freetunnel.client.socks5.constants.HttpTunnelClientConfig;
import cn.t.freetunnel.client.socks5.handler.HttpSocks5TunnelClientHandler;
import cn.t.freetunnel.client.socks5.handler.Socks5TunnelClientMessageHandler;
import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class InitializerBuilder {

    public static NettyTcpChannelInitializer buildHttpProxyServerViaSocks5ClientChannelInitializer() {
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        daemonConfigBuilder.configHandler(Collections.singletonList(ch -> new Socks5TunnelClientMessageHandler()));
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }

    public static NettyTcpChannelInitializer httpSocks5TunnelClientInitializer() {
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //idle
        daemonConfigBuilder.configIdleHandler(HttpTunnelClientConfig.HTTP_PROXY_READ_TIME_OUT_IN_SECONDS, HttpTunnelClientConfig.HTTP_PROXY_WRITE_TIME_OUT_IN_SECONDS, HttpTunnelClientConfig.HTTP_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        List<Function<SocketChannel, ? extends ChannelHandler>> factoryList = new ArrayList<>();
        //HttpServerCodec组合HttpResponseEncoder和HttpRequestDecoder
        //HttpObjectAggregator用来聚合一个HttpMessage和后面的多个HttpContent为一个完整的FullHttpRequest或FullHttpResponse
        //http response encoder
        factoryList.add(ch -> new HttpResponseEncoder());
        //http request decoder
        factoryList.add(ch -> new HttpRequestDecoder());
        factoryList.add(ch -> new HttpSocks5TunnelClientHandler());
        daemonConfigBuilder.configHandler(factoryList);
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        return new NettyTcpChannelInitializer(daemonConfig);
    }
}
