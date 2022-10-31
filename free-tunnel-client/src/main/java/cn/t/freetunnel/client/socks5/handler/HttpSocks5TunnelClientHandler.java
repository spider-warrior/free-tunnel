package cn.t.freetunnel.client.socks5.handler;

import cn.t.freetunnel.client.socks5.listener.HttpSocks5TunnelClientReadyListener;
import cn.t.freetunnel.client.socks5.listener.HttpsSocks5TunnelClientReadyListener;
import cn.t.freetunnel.client.socks5.tunnelprovider.PooledTunnelProvider;
import cn.t.freetunnel.common.constants.Socks5TunnelClientConfig;
import cn.t.freetunnel.common.constants.TunnelBuildResult;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

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

    private final Socks5TunnelClientConfig socks5TunnelClientConfig;

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
        if(httpMethod == HttpMethod.CONNECT) {
            buildHttpsProxy(ctx, targetHost, targetPort, httpVersion);
        } else {
            buildHttpProxy(ctx, targetHost, targetPort, httpVersion, request);
        }
    }

    private void buildHttpsProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion) {
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
        PooledTunnelProvider.acquireSocks5Tunnel(ctx.channel(), targetHost, targetPort, socks5TunnelClientConfig, tunnelBuildResultListener);
    }

    private void buildHttpProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion, FullHttpRequest request) {
        FullHttpRequest proxiedRequest = request.retainedDuplicate();
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                ChannelPromise promise = remoteChannel.newPromise();
                promise.addListener(new HttpSocks5TunnelClientReadyListener(ctx.channel(), remoteChannel, targetHost, targetPort));
                ByteBuf buf = TunnelUtil.httpRequestToByteBuf(ctx.alloc(), proxiedRequest);
                remoteChannel.writeAndFlush(buf, promise);
            } else {
                ReferenceCountUtil.release(proxiedRequest);
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", remoteAddress, targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        PooledTunnelProvider.acquireSocks5Tunnel(ctx.channel(), targetHost, targetPort, socks5TunnelClientConfig, tunnelBuildResultListener);
    }


    public HttpSocks5TunnelClientHandler(Socks5TunnelClientConfig socks5TunnelClientConfig) {
        this.socks5TunnelClientConfig = socks5TunnelClientConfig;
    }
}
