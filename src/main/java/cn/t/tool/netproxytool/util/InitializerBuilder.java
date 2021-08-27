package cn.t.tool.netproxytool.util;

import cn.t.tool.netproxytool.event.ProxyConnectionBuildResultListener;
import cn.t.tool.netproxytool.handler.FetchMessageHandler;
import cn.t.tool.netproxytool.http.config.Socks5ClientConfig;
import cn.t.tool.netproxytool.http.constants.HttpProxyServerClientConfig;
import cn.t.tool.netproxytool.http.constants.HttpProxyServerConfig;
import cn.t.tool.netproxytool.http.server.handler.HttpProxyServerHandler;
import cn.t.tool.netproxytool.http.server.handler.HttpProxyServerViaSocks5Handler;
import cn.t.tool.netproxytool.socks5.client.analyse.MethodResponseAnalyse;
import cn.t.tool.netproxytool.socks5.client.encoder.CmdRequestEncoder;
import cn.t.tool.netproxytool.socks5.client.encoder.MethodRequestEncoder;
import cn.t.tool.netproxytool.socks5.client.encoder.UsernamePasswordAuthenticationRequestEncoder;
import cn.t.tool.netproxytool.socks5.client.handler.AuthenticationResponseHandler;
import cn.t.tool.netproxytool.socks5.client.handler.CmdResponseHandler;
import cn.t.tool.netproxytool.socks5.client.handler.MethodResponseHandler;
import cn.t.tool.netproxytool.socks5.constants.Socks5ClientDaemonConfig;
import cn.t.tool.netproxytool.socks5.constants.Socks5ServerDaemonConfig;
import cn.t.tool.netproxytool.socks5.server.handler.Socks5ServerMessageHandler;
import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyChannelInitializer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class InitializerBuilder {

    public static NettyChannelInitializer buildHttpProxyServerChannelInitializer() {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging handler
        daemonConfigBuilder.configLogLevel(HttpProxyServerConfig.LOGGING_HANDLER_LOGGER_LEVEL);
        //idle handler
        daemonConfigBuilder.configIdleHandler(HttpProxyServerConfig.HTTP_PROXY_READ_TIME_OUT_IN_SECONDS, HttpProxyServerConfig.HTTP_PROXY_WRITE_TIME_OUT_IN_SECONDS, HttpProxyServerConfig.HTTP_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        List<Supplier<? extends ChannelHandler>> supplierList = new ArrayList<>();
        //http request decoder
        supplierList.add(HttpRequestDecoder::new);
        //http response encoder
        supplierList.add(HttpResponseEncoder::new);
        //http message aggregate
        supplierList.add(() -> new HttpObjectAggregator(1024 * 1024));
        //http proxy handler
        supplierList.add(HttpProxyServerHandler::new);
        daemonConfigBuilder.configHandler(supplierList);
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyChannelInitializer(daemonConfig);
    }

    public static NettyChannelInitializer buildHttpFetchMessageClientChannelInitializer(ChannelHandlerContext remoteChannelHandlerContext, ProxyConnectionBuildResultListener proxyConnectionBuildResultListener) {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging
        daemonConfigBuilder.configLogLevel(HttpProxyServerClientConfig.LOGGING_HANDLER_LOGGER_LEVEL);
        //idle
        daemonConfigBuilder.configIdleHandler(HttpProxyServerClientConfig.HTTP_PROXY_READ_TIME_OUT_IN_SECONDS, HttpProxyServerClientConfig.HTTP_PROXY_WRITE_TIME_OUT_IN_SECONDS, HttpProxyServerClientConfig.HTTP_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(() -> new FetchMessageHandler(remoteChannelHandlerContext, proxyConnectionBuildResultListener)));
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyChannelInitializer(daemonConfig);
    }

    public static NettyChannelInitializer buildHttpProxyServerViaSocks5ChannelInitializer(Socks5ClientConfig socks5ClientConfig) {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging
        daemonConfigBuilder.configLogLevel(HttpProxyServerConfig.LOGGING_HANDLER_LOGGER_LEVEL);
        //idle
        daemonConfigBuilder.configIdleHandler(HttpProxyServerConfig.HTTP_PROXY_READ_TIME_OUT_IN_SECONDS, HttpProxyServerConfig.HTTP_PROXY_WRITE_TIME_OUT_IN_SECONDS, HttpProxyServerConfig.HTTP_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        List<Supplier<? extends ChannelHandler>> supplierList = new ArrayList<>();
        //http response encoder
        supplierList.add(HttpResponseEncoder::new);
        //http request decoder
        supplierList.add(HttpRequestDecoder::new);
        supplierList.add(() -> new HttpObjectAggregator(1024 * 1024));
        supplierList.add(() -> new HttpProxyServerViaSocks5Handler(socks5ClientConfig));
        daemonConfigBuilder.configHandler(supplierList);
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyChannelInitializer(daemonConfig);
    }

    public static NettyChannelInitializer buildHttpProxyServerViaSocks5ClientChannelInitializer(ChannelHandlerContext remoteChannelHandlerContext, ProxyConnectionBuildResultListener proxyConnectionBuildResultListener, String targetHost, short targetPort, Socks5ClientConfig socks5ClientConfig) {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging
        daemonConfigBuilder.configLogLevel(Socks5ClientDaemonConfig.LOGGING_HANDLER_LOGGER_LEVEL);
        //idle
        daemonConfigBuilder.configIdleHandler(Socks5ClientDaemonConfig.SOCKS5_PROXY_READ_TIME_OUT_IN_SECONDS, Socks5ClientDaemonConfig.SOCKS5_PROXY_WRITE_TIME_OUT_IN_SECONDS, Socks5ClientDaemonConfig.SOCKS5_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        //negotiate
        daemonConfigBuilder.configByteBufAnalyser(MethodResponseAnalyse::new);
        List<Supplier<? extends MessageToByteEncoder<?>>> encoderSupplierList = new ArrayList<>();
        //socks5 methodRequest encoder
        encoderSupplierList.add(MethodRequestEncoder::new);
        //socks5 usernamePasswordRequest encoder
        encoderSupplierList.add(UsernamePasswordAuthenticationRequestEncoder::new);
        //socks5 cmdRequest encoder
        encoderSupplierList.add(CmdRequestEncoder::new);
        daemonConfigBuilder.configM2bEncoder(encoderSupplierList);
        List<Supplier<? extends ChannelHandler>> handlerSupplierList = new ArrayList<>();
        //negotiate response handler
        handlerSupplierList.add(() -> new MethodResponseHandler(targetHost, targetPort, socks5ClientConfig));
        //authentication response handler
        handlerSupplierList.add(AuthenticationResponseHandler::new);
        //cmd response handler
        handlerSupplierList.add(() -> new CmdResponseHandler(proxyConnectionBuildResultListener, remoteChannelHandlerContext, socks5ClientConfig));
        daemonConfigBuilder.configHandler(handlerSupplierList);
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyChannelInitializer(daemonConfig);
    }

    public static NettyChannelInitializer buildSocks5ProxyServerChannelInitializer() {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging
        daemonConfigBuilder.configLogLevel(Socks5ServerDaemonConfig.LOGGING_HANDLER_LOGGER_LEVEL);
        //idle
        daemonConfigBuilder.configIdleHandler(Socks5ServerDaemonConfig.SOCKS5_PROXY_READ_TIME_OUT_IN_SECONDS, Socks5ServerDaemonConfig.SOCKS5_PROXY_WRITE_TIME_OUT_IN_SECONDS, Socks5ServerDaemonConfig.SOCKS5_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        daemonConfigBuilder.configHandler(Collections.singletonList(Socks5ServerMessageHandler::new));
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        return new NettyChannelInitializer(daemonConfig);
    }
}
