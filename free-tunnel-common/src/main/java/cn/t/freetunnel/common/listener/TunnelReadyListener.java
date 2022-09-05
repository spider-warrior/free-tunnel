package cn.t.freetunnel.common.listener;

import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TunnelReadyListener implements ChannelFutureListener {

    private static final Logger logger = LoggerFactory.getLogger(TunnelReadyListener.class);

    protected final ChannelHandlerContext localChannelHandlerContext;
    protected final ChannelHandlerContext remoteChannelHandlerContext;
    protected final String host;

    @Override
    public void operationComplete(ChannelFuture future) {
        ChannelHandlerContext remoteContextToUse;
        ChannelHandlerContext localContextToUse;
        //通知浏览器成功
        if(future.channel() == localChannelHandlerContext.channel()) {
            remoteContextToUse = remoteChannelHandlerContext;
            localContextToUse = localChannelHandlerContext;
        } else {
            //消息转发成功
            remoteContextToUse = localChannelHandlerContext;
            localContextToUse = remoteChannelHandlerContext;
        }
        if(future.isSuccess()) {
            if(remoteContextToUse.channel().isOpen()) {
                logger.info("代理就位: {}", TunnelUtil.buildProxyTunnelName(remoteChannelHandlerContext, localChannelHandlerContext));
                notifySuccess(future);
            } else {
                if(remoteContextToUse == localChannelHandlerContext) {
                    notifyFailed(localContextToUse, future);
                } else {
                    logger.error("{}[{}]: 代理连接失败: {}, 本地连接状态: {}, 远端连接状态: {}， 即将释放连接: {}", TunnelUtil.buildProxyTunnelName(localChannelHandlerContext, remoteChannelHandlerContext), this.getClass().getSimpleName(), host, localChannelHandlerContext.channel().isOpen(), remoteChannelHandlerContext.channel().isOpen(), localContextToUse.channel());
                    TunnelUtil.closeByRemoteImmediately(localContextToUse);
                }
            }
        } else {
            notifyFailed(remoteContextToUse, future);
        }
    }

    protected abstract void notifySuccess(ChannelFuture future);



    protected void notifyFailed(ChannelHandlerContext ctx, ChannelFuture future) {
        logger.error("{}[{}]: 代理结果通知失败: {}, 即将关闭远程连接, 失败原因: {}", TunnelUtil.buildProxyTunnelName(localChannelHandlerContext, remoteChannelHandlerContext), this.getClass().getSimpleName(), host, future.cause());
        TunnelUtil.closeByRemoteImmediately(ctx);
    }

    public TunnelReadyListener(ChannelHandlerContext localChannelHandlerContext, ChannelHandlerContext remoteChannelHandlerContext, String host) {
        this.localChannelHandlerContext = localChannelHandlerContext;
        this.remoteChannelHandlerContext = remoteChannelHandlerContext;
        this.host = host;
    }
}
