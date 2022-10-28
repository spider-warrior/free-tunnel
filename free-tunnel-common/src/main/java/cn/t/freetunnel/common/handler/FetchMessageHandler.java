package cn.t.freetunnel.common.handler;

import cn.t.freetunnel.common.constants.TunnelBuildResult;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * 主动转发消息处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 20:54
 **/
public class FetchMessageHandler extends ForwardingMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(FetchMessageHandler.class);

    private final TunnelBuildResultListener tunnelBuildResultListener;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        boolean remoteOpen = remoteChannel.isOpen();
        if(remoteOpen) {
            logger.info("[{} -> {}]: 断开连接, 关闭客户端连接: [{} -> {}]", ctx.channel().localAddress(), ctx.channel().remoteAddress(), remoteChannel.remoteAddress(), remoteChannel.localAddress());
            remoteChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        } else {
            logger.info("[{} -> {}]: 断开连接", ctx.channel().localAddress(), ctx.channel().remoteAddress());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        tunnelBuildResultListener.handle(TunnelBuildResult.SUCCEEDED.value, ctx.channel());
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        ctx.connect(remoteAddress, localAddress, promise.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                //连接失败处理
                logger.error("[{}]: 连接失败, 回调监听器", remoteAddress, future.cause());
                tunnelBuildResultListener.handle(TunnelBuildResult.FAILED.value, ctx.channel());
            }
        }));
    }

    public FetchMessageHandler(Channel remoteChannel, TunnelBuildResultListener tunnelBuildResultListener) {
        super(remoteChannel);
        this.tunnelBuildResultListener = tunnelBuildResultListener;
    }
}
