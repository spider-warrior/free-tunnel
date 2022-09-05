package cn.t.freetunnel.common.util;

import cn.t.freetunnel.common.constants.NettyAttrConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * TunnelUtil
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2021-06-23 21:45
 **/
public class TunnelUtil {

    public static void closeGracefully(ChannelHandlerContext ctx) {
        if(ctx.channel().isOpen()) {
            ctx.channel().attr(NettyAttrConstants.CLOSE_BY_CALL_METHOD).set(true);
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void closeImmediately(ChannelHandlerContext ctx) {
        closeImmediately(ctx, null);
    }

    public static void closeImmediately(ChannelHandlerContext ctx, GenericFutureListener<? extends Future<? super Void>> listener) {
        if(ctx.channel().isOpen()) {
            ctx.channel().attr(NettyAttrConstants.CLOSE_BY_CALL_METHOD).set(true);
            if(listener == null) {
                ctx.close();
            } else {
                ctx.close().addListener(listener);
            }

        }
    }

    public static String buildProxyConnectionName(String clientHost, int clientPort, String targetHost, int targetPort) {
        return clientHost + ":" + clientPort + " -> " + targetHost + ":" + targetPort;
    }

    public static String buildProxyTunnelName(ChannelHandlerContext localContext, ChannelHandlerContext remoteContext) {
        Channel localChannel = localContext.channel();
        Channel remoteChannel = remoteContext.channel();
        return remoteChannel.remoteAddress() + " -> " + remoteChannel.localAddress() + " --> " + localChannel.localAddress() + " -> " + localChannel.remoteAddress();
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
            headers.set(HttpHeaderNames.CONNECTION, proxyConnection);
        }
    }

    public static ByteBuf httpRequestToByteBuf(ByteBufAllocator allocator, FullHttpRequest request) {
        prepareProxiedRequest(request);
        return doWriteBuf(allocator, new EmbeddedChannel(new HttpRequestEncoder()), request);
    }

    public static ByteBuf httpResponseToByteBuf(ByteBufAllocator allocator, FullHttpResponse response) {
        return doWriteBuf(allocator, new EmbeddedChannel(new HttpResponseEncoder()), response);
    }

    private static ByteBuf doWriteBuf(ByteBufAllocator allocator, EmbeddedChannel embeddedChannel, Object args) {
        embeddedChannel.writeOutbound(args);
        ByteBuf output = allocator.buffer();
        while (true) {
            ByteBuf buf = embeddedChannel.readOutbound();
            if(buf == null) {
                break;
            } else {
                output.writeBytes(buf);
                ReferenceCountUtil.release(buf);
            }
        }
        embeddedChannel.close();
        return output;
    }

    public static String encryptHost(String host) {
        char[] chars = host.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(i%2 == 0) {
                chars[i] = (char)(chars[i] + 3);
            }
        }
        return new String(chars);
    }

    public static String decryptHost(String host) {
        char[] chars = host.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(i%2 == 0) {
                chars[i] = (char)(chars[i] - 3);
            }
        }
        return new String(chars);
    }
}
