package cn.t.freetunnel.common.listener;

import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TunnelReadyListener implements ChannelFutureListener {

    private static final Logger logger = LoggerFactory.getLogger(TunnelReadyListener.class);

    protected final Channel localChannel;
    protected final Channel remoteChannel;
    protected final String host;
    protected final int port;

    @Override
    public void operationComplete(ChannelFuture future) {
        Channel remoteChannelToUse;
        Channel localChannelToUse;
        //通知浏览器成功
        if(future.channel() == localChannel) {
            remoteChannelToUse = remoteChannel;
            localChannelToUse = localChannel;
        } else {
            //消息转发成功
            remoteChannelToUse = localChannel;
            localChannelToUse = remoteChannel;
        }
        if(future.isSuccess()) {
            if(remoteChannelToUse.isOpen()) {
                logger.info("代理就位: {}", TunnelUtil.buildProxyTunnelName(remoteChannel, localChannel));
                notifySuccess(future);
            } else {
                if(remoteChannelToUse == localChannel) {
                    notifyFailed(localChannelToUse, future);
                } else {
                    logger.error("{}[{}]: 代理连接失败: {}:{}, 本地连接状态: {}, 远端连接状态: {}， 即将释放连接: {}", TunnelUtil.buildProxyTunnelName(localChannel, remoteChannel), this.getClass().getSimpleName(), host, port, localChannel.isOpen(), remoteChannel.isOpen(), localChannelToUse);
                    TunnelUtil.closeImmediately(localChannelToUse);
                }
            }
        } else {
            notifyFailed(remoteChannelToUse, future);
        }
    }

    protected abstract void notifySuccess(ChannelFuture future);



    protected void notifyFailed(Channel channel, ChannelFuture future) {
        logger.error("{}[{}]: 代理结果通知失败: {}:{}, 即将关闭远程连接, 失败原因: {}", TunnelUtil.buildProxyTunnelName(localChannel, remoteChannel), this.getClass().getSimpleName(), host, port, future.cause());
        TunnelUtil.closeImmediately(channel);
    }

    public TunnelReadyListener(Channel localChannel, Channel remoteChannel, String host, int port) {
        this.localChannel = localChannel;
        this.remoteChannel = remoteChannel;
        this.host = host;
        this.port = port;
    }
}
