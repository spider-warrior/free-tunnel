package cn.t.freetunnel.server.socks5.handler;

import cn.t.freetunnel.common.constants.FreeTunnelConstants;
import cn.t.freetunnel.common.constants.NettyHandlerName;
import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.exception.TunnelException;
import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Socks5ServerInternalCommandHandler
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-11 16:07
 **/
public class Socks5TunnelServerCommandHandler extends SimpleChannelInboundHandler<TunnelCommand> {

    private static final Logger logger = LoggerFactory.getLogger(FreeTunnelConstants.TUNNEL_EVENT_LOGGER_NAME);

    private volatile Channel remoteChannel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TunnelCommand command) {
        if(TunnelCommand.RESET_STATUS_TO_COMMAND_REQUEST == command) {
            logger.info("客户端请求复位连接,channel: {}", ctx.channel());
            TunnelUtil.closeImmediately(remoteChannel);
            resetStatusToCommand(ctx);
            ctx.writeAndFlush(TunnelCommand.RESET_STATUS_TO_COMMAND_RESPONSE);
        } else if(TunnelCommand.RESET_STATUS_TO_COMMAND_RESPONSE == command) {
            logger.info("客户端已复位连接,channel: {}", ctx.channel());
            resetStatusToCommand(ctx);
        } else if(TunnelCommand.HEART_BEAT == command) {
            logger.info("心跳包: {}", ctx.channel());
        } else {
            throw new TunnelException("不支持的命令: " + command);
        }
    }

    private void resetStatusToCommand(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        Socks5TunnelServerMessageHandler socks5TunnelServerMessageHandler = pipeline.remove(Socks5TunnelServerMessageHandler.class);
        pipeline.addBefore(NettyHandlerName.SOCKS5_TUNNEL_SERVER_FORWARDING_MESSAGE_HANDLER, "socks5ProxyServerMessageHandler", socks5TunnelServerMessageHandler);
    }

    public Channel getRemoteChannel() {
        return remoteChannel;
    }

    public void setRemoteChannel(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    public Socks5TunnelServerCommandHandler(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }
}
