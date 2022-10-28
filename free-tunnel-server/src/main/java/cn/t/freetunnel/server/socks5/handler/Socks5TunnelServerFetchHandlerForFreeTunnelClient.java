package cn.t.freetunnel.server.socks5.handler;

import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 转发消息处理器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 23:46
 **/
public class Socks5TunnelServerFetchHandlerForFreeTunnelClient extends Socks5TunnelServerFetchHandler {

    private static final Logger logger = LoggerFactory.getLogger(Socks5TunnelServerFetchHandlerForFreeTunnelClient.class);

    @Override
    protected void closeRemote(ChannelHandlerContext ctx) {
        logger.info("[{} -> {}]: 断开连接, 复位通道连接: [{} -> {}]", ctx.channel().localAddress(), ctx.channel().remoteAddress(), remoteChannel.remoteAddress(), remoteChannel.localAddress());
        remoteChannel.writeAndFlush(TunnelCommand.RESET_STATUS_TO_COMMAND_REQUEST);
    }

    public Socks5TunnelServerFetchHandlerForFreeTunnelClient(Channel remoteChannel, TunnelBuildResultListener tunnelBuildResultListener) {
        super(remoteChannel, tunnelBuildResultListener);
    }
}
