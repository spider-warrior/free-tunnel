package cn.t.tool.netproxytool.http.server.handler;

import cn.t.tool.netproxytool.event.ProxyConnectionBuildResultListener;
import cn.t.tool.netproxytool.http.config.Socks5ClientConfig;
import cn.t.tool.netproxytool.http.constants.ProxyBuildExecutionStatus;
import cn.t.tool.netproxytool.http.server.listener.HttpProxyServerViaSocks5ClientConnectionReadyListener;
import cn.t.tool.netproxytool.http.server.listener.HttpsProxyServerViaSocks5ClientConnectionReadyListener;
import cn.t.tool.netproxytool.util.NetProxyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_GATEWAY;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * http请求处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-24 11:54
 **/
public class HttpProxyServerViaSocks5Handler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyServerViaSocks5Handler.class);

    private final Socks5ClientConfig socks5ClientConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpMethod httpMethod = request.method();
        String host = request.headers().get(HttpHeaderNames.HOST);
        String[] elements = host.split(":");
        String targetHost = elements[0];
        short targetPort;
        if(elements.length == 1) {
            targetPort = 80;
        } else {
            targetPort= Short.parseShort(elements[1]);
        }
        HttpVersion httpVersion = request.protocolVersion();
        if(httpMethod == HttpMethod.CONNECT) {
            buildHttpsProxy(ctx, targetHost, targetPort, httpVersion);
        } else {
            buildHttpProxy(ctx, targetHost, targetPort, httpVersion, request);
        }
    }

    private void buildHttpsProxy(ChannelHandlerContext ctx, String targetHost, short targetPort, HttpVersion httpVersion) {
        InetSocketAddress clientAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        String clientName = NetProxyUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        ProxyConnectionBuildResultListener proxyConnectionBuildResultListener = (status, remoteChannelHandlerContext) -> {
            if(ProxyBuildExecutionStatus.SUCCEEDED.value == status) {
                logger.info("[{}:{}]: 代理创建成功, remote: {}:{}", clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
                ChannelPromise promise = ctx.newPromise();
                promise.addListener(new HttpsProxyServerViaSocks5ClientConnectionReadyListener(ctx, remoteChannelHandlerContext, clientName));
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, OK), promise);
            } else {
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", clientAddress, targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY));
                ctx.close();
            }
        };
        //连接socks5服务器
        NetProxyUtil.startSocks5ProxyClient(ctx, proxyConnectionBuildResultListener, clientName, targetHost, targetPort, socks5ClientConfig);
    }

    private void buildHttpProxy(ChannelHandlerContext ctx, String targetHost, short targetPort, HttpVersion httpVersion, FullHttpRequest request) {
        InetSocketAddress clientAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        String clientName = NetProxyUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        FullHttpRequest proxiedRequest = request.retainedDuplicate();
        ProxyConnectionBuildResultListener proxyConnectionBuildResultListener = (status, remoteChannelHandlerContext) -> {
            if(ProxyBuildExecutionStatus.SUCCEEDED.value == status) {
                logger.info("[{}:{}]: 代理创建成功, remote: {}:{}", clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
                ChannelPromise promise = remoteChannelHandlerContext.newPromise();
                promise.addListener(new HttpProxyServerViaSocks5ClientConnectionReadyListener(ctx, remoteChannelHandlerContext, clientName));
                ByteBuf buf = NetProxyUtil.httpRequestToByteBuf(proxiedRequest);
                remoteChannelHandlerContext.channel().writeAndFlush(buf, promise);
            } else {
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", clientAddress, targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY));
                ctx.close();
            }
        };
        NetProxyUtil.startSocks5ProxyClient(ctx, proxyConnectionBuildResultListener, clientName, targetHost, targetPort, socks5ClientConfig);
    }


    public HttpProxyServerViaSocks5Handler(Socks5ClientConfig socks5ClientConfig) {
        this.socks5ClientConfig = socks5ClientConfig;
    }
}
