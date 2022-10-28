package cn.t.freetunnel.common.handler;

import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 被动转发消息处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 23:46
 **/
public class ForwardingMessageHandler extends ChannelDuplexHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Channel remoteChannel;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg instanceof ByteBuf) {
            logger.debug("[{}] -> [{}] -> [{}] -> [{}]: 转发byteBuf消息: {} B", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannel.localAddress(), remoteChannel.remoteAddress(), ((ByteBuf)msg).readableBytes());
            remoteChannel.writeAndFlush(msg);
        } else if(msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest)msg;
            ByteBuf buf = TunnelUtil.httpRequestToByteBuf(ctx.alloc(), request);
            logger.debug("[{}] -> [{}] -> [{}] -> [{}]: 转发request消息: {} B", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannel.localAddress(), remoteChannel.remoteAddress(), buf.readableBytes());
            remoteChannel.writeAndFlush(buf);
        } else if(msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse)msg;
            ByteBuf buf = TunnelUtil.httpResponseToByteBuf(ctx.alloc(), response);
            logger.debug("[{}] -> [{}] -> [{}] -> [{}]: 转发response消息: {} B", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannel.localAddress(), remoteChannel.remoteAddress(), buf.readableBytes());
            remoteChannel.writeAndFlush(buf);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        boolean remoteOpen = remoteChannel.isOpen();
        if(remoteOpen) {
            logger.info("[{} -> {}]: 断开连接, 释放代理资源: [{} -> {}]", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannel.localAddress(), remoteChannel.remoteAddress());
            remoteChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        } else {
            logger.info("[{} -> {}]: 断开连接", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        }
    }

    public Channel getRemoteChannel() {
        return remoteChannel;
    }

    public void setRemoteChannel(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    public ForwardingMessageHandler(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }
}
