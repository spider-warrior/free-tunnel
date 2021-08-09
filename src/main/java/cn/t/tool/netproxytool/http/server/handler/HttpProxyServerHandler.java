package cn.t.tool.netproxytool.http.server.handler;

import cn.t.tool.netproxytool.event.ProxyConnectionBuildResultListener;
import cn.t.tool.netproxytool.http.constants.ProxyBuildExecutionStatus;
import cn.t.tool.netproxytool.http.server.listener.ProxyServerConnectionReadyListener;
import cn.t.tool.netproxytool.util.NetProxyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
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
public class HttpProxyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyServerHandler.class);

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
        InetSocketAddress clientAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        String clientName = NetProxyUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        ProxyConnectionBuildResultListener proxyConnectionBuildResultListener = (status, remoteChannelHandlerContext) -> {
            if(ProxyBuildExecutionStatus.SUCCEEDED.value == status) {
                logger.info("[client: {}] -> [local: {}] -> [remote: {}]: 代理创建成功", ctx.channel().remoteAddress(), remoteChannelHandlerContext.channel().localAddress(), remoteChannelHandlerContext.channel().remoteAddress());
                ChannelPromise promise = ctx.newPromise();
                promise.addListener(new ProxyServerConnectionReadyListener(ctx, remoteChannelHandlerContext, clientName));
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, OK), promise);
            } else {
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", clientAddress, targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY));
                ctx.close();
            }
        };
        NetProxyUtil.startHttpFetchMessageClient(ctx, proxyConnectionBuildResultListener, clientName, targetHost, targetPort);
    }

    private void buildHttpProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion, FullHttpRequest request) {
        InetSocketAddress clientAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        String clientName = NetProxyUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
        FullHttpRequest proxiedRequest = request.retainedDuplicate();
        ProxyConnectionBuildResultListener proxyConnectionBuildResultListener = (status, remoteChannelHandlerContext) -> {
            if(ProxyBuildExecutionStatus.SUCCEEDED.value == status) {
                logger.info("[client: {}] -> [local: {}] -> [remote: {}]: 代理创建成功", ctx.channel().remoteAddress(), remoteChannelHandlerContext.channel().localAddress(), remoteChannelHandlerContext.channel().remoteAddress());
                ChannelPromise promise = remoteChannelHandlerContext.newPromise();
                promise.addListener(new ProxyServerConnectionReadyListener(ctx, remoteChannelHandlerContext, clientName));
                NetProxyUtil.prepareProxiedRequest(proxiedRequest);
                EmbeddedChannel embeddedChannel = new EmbeddedChannel(new HttpRequestEncoder());
                embeddedChannel.writeOutbound(proxiedRequest);
                ByteBuf byteBuf = embeddedChannel.readOutbound();
                embeddedChannel.close();
                remoteChannelHandlerContext.channel().writeAndFlush(byteBuf, promise);
            } else {
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", clientAddress, targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY));
                ctx.close();
            }
        };
        NetProxyUtil.startHttpFetchMessageClient(ctx, proxyConnectionBuildResultListener, clientName, targetHost, targetPort);
    }

}
