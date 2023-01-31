package cn.t.freetunnel.common.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
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
        } else if(msg instanceof ByteBufHolder) {
            logger.debug("[{}] -> [{}] -> [{}] -> [{}]: 转发ByteBufHolder消息: {} B", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannel.localAddress(), remoteChannel.remoteAddress(), ((ByteBufHolder)msg).content().readableBytes());
            remoteChannel.writeAndFlush(msg);
        } else if(msg instanceof HttpRequest) {
            logger.debug("[{}] -> [{}] -> [{}] -> [{}]: 转发ByteBufHolder消息: {}", ctx.channel().remoteAddress(), ctx.channel().localAddress(), remoteChannel.localAddress(), remoteChannel.remoteAddress(), msg);
            remoteChannel.writeAndFlush(msg);
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
