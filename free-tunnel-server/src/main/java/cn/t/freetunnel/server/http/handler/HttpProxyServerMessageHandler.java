package cn.t.freetunnel.server.http.handler;

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

import java.util.LinkedList;
import java.util.Queue;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_GATEWAY;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * http请求处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-24 11:54
 **/
public class HttpProxyServerMessageHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyServerMessageHandler.class);
    private final Queue<HttpObject> cachedHttpObjectList = new LinkedList<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) {
        if(httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest)httpObject;
            HttpMethod httpMethod = request.method();
            HttpVersion httpVersion = request.protocolVersion();
            if(httpMethod == HttpMethod.CONNECT) {
                String uri = request.uri();
                String[] elements = uri.split(":");
                if(elements.length == 1) {
                    buildHttpsProxy(ctx, elements[0], 80, httpVersion);
                } else {

                    buildHttpsProxy(ctx, elements[0], Integer.parseInt(elements[1]), httpVersion);
                }
            } else {
                String host = request.headers().get(HttpHeaderNames.HOST);
                String[] elements = host.split(":");
                if(elements.length == 1) {
                    buildHttpProxy(ctx, elements[0], 80, httpVersion, request);
                } else {
                    buildHttpProxy(ctx, elements[0], Integer.parseInt(elements[1]), httpVersion, request);
                }
            }
        } else {
            cachedHttpObjectList.add(httpObject);
        }
    }

    private void buildHttpsProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion) {
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                ChannelPromise promise = ctx.newPromise();
                promise.addListener(new HttpsTunnelReadyListener(remoteChannel, targetHost, targetPort, this));
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, OK), promise);
            } else {
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", ctx.channel().remoteAddress(), targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        UnPooledTunnelProvider.acquireTcpTunnelForHttps(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
    }

    private void buildHttpProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion, HttpRequest request) {
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                ChannelPromise promise = remoteChannel.newPromise();
                promise.addListener(new HttpTunnelReadyListener(ctx.channel(), targetHost, targetPort, this));
                remoteChannel.write(request, promise);
                if(cachedHttpObjectList.size() == 1) {
                    remoteChannel.write(cachedHttpObjectList.poll());
                } else {
                    while (true) {
                        HttpObject httpObject = cachedHttpObjectList.poll();
                        if(httpObject == null) {
                            break;
                        } else {
                            remoteChannel.write(httpObject);
                        }
                    }
                }
                remoteChannel.flush();
            } else {
                ReferenceCountUtil.release(request);
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", ctx.channel().remoteAddress(), targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        UnPooledTunnelProvider.acquireTcpTunnelForHttp(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
    }

}
