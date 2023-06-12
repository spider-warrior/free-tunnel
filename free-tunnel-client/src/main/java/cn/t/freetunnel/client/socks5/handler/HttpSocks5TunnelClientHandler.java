package cn.t.freetunnel.client.socks5.handler;

import cn.t.freetunnel.client.socks5.listener.HttpSocks5TunnelClientReadyListener;
import cn.t.freetunnel.client.socks5.listener.HttpsSocks5TunnelClientReadyListener;
import cn.t.freetunnel.client.socks5.tunnelprovider.StaticChannelProvider;
import cn.t.freetunnel.client.socks5.util.SocketUtil;
import cn.t.freetunnel.common.constants.TunnelBuildResult;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import cn.t.freetunnel.server.http.listener.HttpTunnelReadyListener;
import cn.t.freetunnel.server.http.listener.HttpsTunnelReadyListener;
import cn.t.freetunnel.server.tunnelprovider.UnPooledTunnelProvider;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Queue;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_GATEWAY;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * http代理请求处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-24 11:54
 **/
public class HttpSocks5TunnelClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = LoggerFactory.getLogger(HttpSocks5TunnelClientHandler.class);
    private final Queue<Object> cachedHttpObjectList = new LinkedList<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) {
        if(httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest)httpObject;
            HttpMethod httpMethod = request.method();
            String host;
            if(httpMethod == HttpMethod.CONNECT) {
                host = request.uri();
            } else {
                host = request.headers().get(HttpHeaderNames.HOST);
                if(host == null) {
                    if(request.uri().contains("://")) {
                        host = request.uri().substring(request.uri().indexOf("://") + 3);
                        int slashIndex = host.indexOf("/");
                        if(slashIndex > -1) {
                            host = host.substring(0, slashIndex);
                        }
                    } else {
                        throw new RuntimeException("无法解析host,uri: " + request.uri());
                    }
                }
            }
            String[] elements = host.split(":");
            String targetHost = elements[0];
            int targetPort;
            if(elements.length == 1) {
                targetPort = (httpMethod == HttpMethod.CONNECT) ? 443 : 80;
            } else {
                targetPort= Integer.parseInt(elements[1]);
            }
            logger.info("收到请求, 开始构建代理, 本地channel: {}, targetHost: {}, targetPort: {}", ctx.channel(), targetHost, targetPort);
            if(SocketUtil.isSiteLocalAddress(targetHost)) {
                buildDirectProxy(ctx, httpMethod, targetHost, targetPort, request.protocolVersion(), request);
            } else {
                buildSocks5Proxy(ctx, httpMethod, targetHost, targetPort, request.protocolVersion(), request);
            }
        } else {
            if(httpObject instanceof ByteBufHolder) {
                cachedHttpObjectList.add(((ByteBufHolder)httpObject).retain());
            } else {
                cachedHttpObjectList.add(httpObject);
            }
        }
    }

    private void buildDirectProxy(ChannelHandlerContext ctx, HttpMethod httpMethod, String targetHost, int targetPort, HttpVersion httpVersion, HttpRequest request) {
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
                promise.addListener(new HttpsTunnelReadyListener(remoteChannel, targetHost, targetPort, this));
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, OK), promise);
            } else {
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", ctx.channel().remoteAddress(), targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        UnPooledTunnelProvider.acquireTcpTunnelForHttps(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
    }

    private void writeAndFlushCachedMessage(Channel channel, ChannelPromise promise) {
        if(cachedHttpObjectList.size() == 1) {
            channel.writeAndFlush(cachedHttpObjectList.poll(), promise);
        } else {
            while (true) {
                Object cachedMsg = cachedHttpObjectList.poll();
                if(cachedMsg == null) {
                    break;
                } else {
                    channel.write(cachedMsg);
                }
            }
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER, promise);
        }
    }

    private void buildDirectHttpProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion, HttpRequest request) {
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                ChannelPromise promise = remoteChannel.newPromise();
                promise.addListener(new HttpTunnelReadyListener(ctx.channel(), targetHost, targetPort, this));
                remoteChannel.write(request);
                writeAndFlushCachedMessage(remoteChannel, promise);
            } else {
                ReferenceCountUtil.release(request);
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", ctx.channel().remoteAddress(), targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        UnPooledTunnelProvider.acquireTcpTunnelForHttp(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
    }

    private void buildSocks5Proxy(ChannelHandlerContext ctx, HttpMethod httpMethod, String targetHost, int targetPort, HttpVersion httpVersion, HttpRequest request) {
        if(httpMethod == HttpMethod.CONNECT) {
            buildSocks5HttpsProxy(ctx, targetHost, targetPort, httpVersion);
        } else {
            buildSocks5HttpProxy(ctx, targetHost, targetPort, httpVersion, request);
        }
    }

    private void buildSocks5HttpsProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion) {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                ChannelPromise promise = ctx.newPromise();
                promise.addListener(new HttpsSocks5TunnelClientReadyListener(remoteChannel, targetHost, targetPort));
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, OK), promise);
            } else {
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", remoteAddress, targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        //请求socks5通道
        StaticChannelProvider.acquireSocks5Tunnel(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
    }

    private void buildSocks5HttpProxy(ChannelHandlerContext ctx, String targetHost, int targetPort, HttpVersion httpVersion, HttpRequest request) {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        TunnelBuildResultListener tunnelBuildResultListener = (status, remoteChannel) -> {
            if(TunnelBuildResult.SUCCEEDED.value == status) {
                ChannelPromise promise = remoteChannel.newPromise();
                promise.addListener(new HttpSocks5TunnelClientReadyListener(ctx.channel(), targetHost, targetPort));
                remoteChannel.write(request);
                writeAndFlushCachedMessage(remoteChannel, promise);
            } else {
                ReferenceCountUtil.release(request);
                logger.error("[{}]: 代理客户端失败, remote: {}:{}", remoteAddress, targetHost, targetPort);
                ctx.writeAndFlush(new DefaultFullHttpResponse(httpVersion, BAD_GATEWAY)).addListener(ChannelFutureListener.CLOSE);
            }
        };
        StaticChannelProvider.acquireSocks5Tunnel(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
    }


    public HttpSocks5TunnelClientHandler() {}
}
