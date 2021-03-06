package cn.t.tool.netproxytool.handler;

import cn.t.tool.netproxytool.exception.ProxyException;
import cn.t.tool.netproxytool.util.NetProxyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 转发消息处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 23:46
 **/
public class ForwardingMessageHandler extends ChannelDuplexHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final ChannelHandlerContext remoteChannelHandlerContext;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg instanceof ByteBuf) {
            log.info("[{}] -> [{}] -> [{}] -> [{}]: 转发byteBuf消息: {} B", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannelHandlerContext.channel().localAddress(), remoteChannelHandlerContext.channel().remoteAddress(), ((ByteBuf)msg).readableBytes());
            remoteChannelHandlerContext.channel().writeAndFlush(msg);
        } else if(msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest)msg;
            ByteBuf buf = NetProxyUtil.httpRequestToByteBuf(request);
            remoteChannelHandlerContext.channel().writeAndFlush(buf);
//            log.info("[{}] -> [{}] -> [{}] -> [{}]: 转发request消息:\r\n{}", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannelHandlerContext.channel().localAddress(), remoteChannelHandlerContext.channel().remoteAddress(), msg);
            log.info("[{}] -> [{}] -> [{}] -> [{}]: 转发request消息: {} B", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannelHandlerContext.channel().localAddress(), remoteChannelHandlerContext.channel().remoteAddress(), buf.readableBytes());
        } else if(msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse)msg;
            ByteBuf buf = NetProxyUtil.httpResponseToByteBuf(response);
            remoteChannelHandlerContext.channel().writeAndFlush(buf);
//            log.info("[{}] -> [{}] -> [{}] -> [{}]: 转发response消息:\r\n{}", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannelHandlerContext.channel().localAddress(), remoteChannelHandlerContext.channel().remoteAddress(), msg);
            log.info("[{}] -> [{}] -> [{}] -> [{}]: 转发response消息: {} B", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannelHandlerContext.channel().localAddress(), remoteChannelHandlerContext.channel().remoteAddress(), buf.readableBytes());
        } else {
            throw new ProxyException("不支持的转发消息: " + msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        boolean remoteOpen = remoteChannelHandlerContext.channel().isOpen();
        if(this.getClass().equals(ForwardingMessageHandler.class)) {
            if(remoteOpen) {
                log.info("[{} -> {}]: 断开连接, 释放代理资源", ctx.channel().remoteAddress(), ctx.channel().localAddress());
                remoteChannelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            } else {
                log.info("[{} -> {}]: 断开连接", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            }
        } else {
            if(remoteOpen) {
                log.info("[{} -> {}]: 断开连接, 关闭客户端连接", ctx.channel().localAddress(), ctx.channel().remoteAddress());
                remoteChannelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            } else {
                log.info("[{} -> {}]: 断开连接", ctx.channel().localAddress(), ctx.channel().remoteAddress());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //消息读取失败不能实现消息转发，断开客户端代理
        log.error("代理消息异常, 即将关闭连接: [{} -> {}]", ctx.channel().localAddress(), ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

    public ForwardingMessageHandler(ChannelHandlerContext remoteChannelHandlerContext) {
        this.remoteChannelHandlerContext = remoteChannelHandlerContext;
    }
}
