package cn.t.tool.netproxytool.util;

import cn.t.tool.netproxytool.event.ProxyConnectionBuildResultListener;
import cn.t.tool.netproxytool.http.config.Socks5ClientConfig;
import cn.t.tool.nettytool.daemon.client.NettyTcpClient;
import cn.t.tool.nettytool.initializer.NettyChannelInitializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;

/**
 * @author <a href="mailto:yangjian@ifenxi.com">研发部-杨建</a>
 * @version V1.0
 * @since 2021-06-23 21:45
 **/
public class NetProxyUtil {

    public static String buildProxyConnectionName(String clientHost, int clientPort, String targetHost, int targetPort) {
        return clientHost + ":" + clientPort + " -> " + targetHost + ":" + targetPort;
    }
    public static void startHttpFetchMessageClient(ChannelHandlerContext localContext,
                                                   ProxyConnectionBuildResultListener proxyConnectionBuildResultListener,
                                                   String clientName,
                                                   String targetHost,
                                                   int targetPort) {
        NettyChannelInitializer channelInitializer = InitializerBuilder.buildHttpFetchMessageClientChannelInitializer(localContext, proxyConnectionBuildResultListener);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, targetHost, targetPort, channelInitializer);
        ThreadUtil.submitProxyTask(nettyTcpClient::start);
    }

    public static void startSocks5ProxyClient(ChannelHandlerContext localContext,
                                              ProxyConnectionBuildResultListener proxyConnectionBuildResultListener,
                                              String clientName,
                                              String targetHost,
                                              short targetPort,
                                              Socks5ClientConfig socks5ClientConfig
                                              ) {
        NettyChannelInitializer channelInitializer = InitializerBuilder.buildHttpProxyServerViaSocks5ClientChannelInitializer(localContext, proxyConnectionBuildResultListener, targetHost, targetPort, socks5ClientConfig);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, socks5ClientConfig.getSocks5ServerHost(), socks5ClientConfig.getSocks5ServerPort(), channelInitializer);
        ThreadUtil.submitProxyTask(nettyTcpClient::start);
    }
    public static void prepareProxiedRequest(FullHttpRequest proxiedRequest) {
        String uri = proxiedRequest.uri();
        if(uri.startsWith("http://")) {
            uri = uri.replace("http://", "");
            int slashIndex = uri.indexOf("/");
            if(slashIndex > -1) {
                uri = uri.substring(slashIndex);
                proxiedRequest.setUri(uri);
            }
        }
        HttpHeaders headers = proxiedRequest.headers();
        String proxyConnection = headers.get("Proxy-Connection");
        if(proxyConnection != null) {
            headers.remove("Proxy-Connection");
            headers.set("Connection", proxyConnection);
        }
    }

    public static ByteBuf httpRequestToByteBuf(FullHttpRequest request) {
        NetProxyUtil.prepareProxiedRequest(request);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new HttpRequestEncoder());
        embeddedChannel.writeOutbound(request);
        ByteBuf output = ByteBufAllocator.DEFAULT.buffer();
        while (true) {
            ByteBuf buf = embeddedChannel.readOutbound();
            if(buf == null) {
                break;
            } else {
                output.writeBytes(buf);
            }
        }
        embeddedChannel.close();
        return output;
    }

    public static ByteBuf httpResponseToByteBuf(FullHttpResponse response) {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new HttpResponseEncoder());
        embeddedChannel.writeOutbound(response);
        ByteBuf output = ByteBufAllocator.DEFAULT.buffer();
        while (true) {
            ByteBuf buf = embeddedChannel.readOutbound();
            if(buf == null) {
                break;
            } else {
                output.writeBytes(buf);
            }
        }
        embeddedChannel.close();
        return output;
    }

}
