package cn.t.freetunnel.client.socks5.handler;

import cn.t.freetunnel.client.socks5.listener.HttpSocks5TunnelClientReadyListener;
import cn.t.freetunnel.client.socks5.listener.HttpsSocks5TunnelClientReadyListener;
import cn.t.freetunnel.client.socks5.tunnelprovider.StaticChannelProvider;
import cn.t.freetunnel.common.constants.TunnelBuildResult;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import cn.t.freetunnel.server.http.listener.HttpTunnelReadyListener;
import cn.t.freetunnel.server.http.listener.HttpsTunnelReadyListener;
import cn.t.freetunnel.server.tunnelprovider.UnPooledTunnelProvider;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_GATEWAY;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * http请求处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-24 11:54
 **/
public class HttpSocks5TunnelClientHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpSocks5TunnelClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpMethod httpMethod = request.method();
        String host = request.headers().get(HttpHeaderNames.HOST);
        String[] elements = host.split(":");
        String targetHost = elements[0];
        int targetPort;
        if(elements.length == 1) {
            if(request.uri().startsWith("https")) {
                targetPort = 443;
            } else {
                targetPort = 80;
            }
        } else {
            targetPort= Integer.parseInt(elements[1]);
        }
        HttpVersion httpVersion = request.protocolVersion();
        buildProxy(ctx, httpMethod, targetHost, targetPort, httpVersion, request);
    }

    private void buildProxy(ChannelHandlerContext ctx, HttpMethod httpMethod, String targetHost, int targetPort, HttpVersion httpVersion, FullHttpRequest request) {
        logger.info("收到请求, 开始构建代理, 本地channel: {}, targetHost: {}, targetPort: {}", ctx.channel(), targetHost, targetPort);
        try {
            InetAddress inetAddress = InetAddress.getByName(targetHost);
            if(inetAddress.isSiteLocalAddress()) {
                buildDirectProxy(ctx, httpMethod, targetHost, targetPort, httpVersion, request);
            } else {
                buildSocks5Proxy(ctx, httpMethod, targetHost, targetPort, httpVersion, request);
            }
        } catch (UnknownHostException e) {
            buildSocks5Proxy(ctx, httpMethod, targetHost, targetPort, httpVersion, request);
        }
    }

    private void buildSocks5Proxy(ChannelHandlerContext ctx, HttpMethod httpMethod, String targetHost, int targetPort, HttpVersion httpVersion, FullHttpRequest request) {
        if(httpMethod == HttpMethod.CONNECT) {
            buildSocks5HttpsProxy(ctx, targetHost, targetPort, httpVersion);
        } else {
            buildSocks5HttpProxy(ctx, targetHost, targetPort, httpVersion, request);
        }
    }

    private void buildDirectProxy(ChannelHandlerContext ctx, HttpMethod httpMethod, String targetHost, int targetPort, HttpVersion httpVersion, FullHttpRequest request) {
        if(httpMethod == HttpMethod.CONNECT) {
            buildDirectHttpsProxy(ctx, targetHost, targetPort, httpVersion);
        } else {
            buildDirectHttpProxy(ctx, targetHost, targetPort, httpVersion, request);
        }
    }

    private void buildDirectHttpsProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion) {
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                ChannelPromise promise = ctx.newPromise();
                promise.addListener(new HttpsTunnelReadyListener(ctx.channel(), remoteChannel, targetHost, targetPort));
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, OK), promise);
            } else {
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", ctx.channel().remoteAddress(), targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        UnPooledTunnelProvider.acquireTcpTunnelForHttps(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
    }

    private void buildDirectHttpProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion, FullHttpRequest request) {
        FullHttpRequest proxiedRequest = request.retain();
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                ChannelPromise promise = remoteChannel.newPromise();
                promise.addListener(new HttpTunnelReadyListener(remoteChannel, ctx.channel(), targetHost, targetPort, this));
                remoteChannel.writeAndFlush(proxiedRequest, promise);
            } else {
                ReferenceCountUtil.release(proxiedRequest);
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", ctx.channel().remoteAddress(), targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        UnPooledTunnelProvider.acquireTcpTunnelForHttp(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
    }

    private void buildSocks5HttpsProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion) {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                ChannelPromise promise = ctx.newPromise();
                promise.addListener(new HttpsSocks5TunnelClientReadyListener(ctx.channel(), remoteChannel, targetHost, targetPort));
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, OK), promise);
            } else {
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", remoteAddress, targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        //请求socks5通道
        StaticChannelProvider.acquireSocks5Tunnel(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
    }

    private void buildSocks5HttpProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion, FullHttpRequest request) {
        FullHttpRequest proxiedRequest = request.retain();
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                ChannelPromise promise = remoteChannel.newPromise();
                promise.addListener(new HttpSocks5TunnelClientReadyListener(remoteChannel, ctx.channel(), targetHost, targetPort));
                remoteChannel.writeAndFlush(proxiedRequest, promise);
            } else {
                ReferenceCountUtil.release(proxiedRequest);
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", remoteAddress, targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        StaticChannelProvider.acquireSocks5Tunnel(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
    }


    public HttpSocks5TunnelClientHandler() {}
}
