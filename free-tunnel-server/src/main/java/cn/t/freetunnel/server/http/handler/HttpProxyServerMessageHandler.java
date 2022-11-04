package cn.t.freetunnel.server.http.handler;

import cn.t.freetunnel.common.constants.TunnelBuildResult;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import cn.t.freetunnel.common.util.TunnelUtil;
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

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_GATEWAY;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * http请求处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-24 11:54
 **/
public class HttpProxyServerMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyServerMessageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpMethod httpMethod = request.method();
        String host = request.headers().get(HttpHeaderNames.HOST);
        String[] elements = host.split(":");
        String targetHost = elements[0];
        int targetPort;
        if(elements.length == 1) {
            targetPort = 80;
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

    private void buildHttpProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion, FullHttpRequest request) {
        FullHttpRequest proxiedRequest = request.retainedDuplicate();
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                TunnelUtil.prepareProxiedRequest(proxiedRequest);
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

}
