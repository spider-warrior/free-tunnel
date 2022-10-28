package cn.t.freetunnel.client.socks5.handler;

import cn.t.freetunnel.client.socks5.tunnelprovider.PooledTunnelProvider;
import cn.t.freetunnel.common.constants.FreeTunnelConstants;
import cn.t.freetunnel.common.constants.NettyHandlerName;
import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.exception.TunnelException;
import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Socks5TunnelClientCommandHandler
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-11 16:07
 **/
public class Socks5TunnelClientCommandHandler extends SimpleChannelInboundHandler<TunnelCommand> {

    private static final Logger logger = LoggerFactory.getLogger(FreeTunnelConstants.TUNNEL_EVENT_LOGGER_NAME);

    private volatile Channel remoteChannel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TunnelCommand command) {
        if(TunnelCommand.RESET_STATUS_TO_COMMAND_REQUEST == command) {
            logger.info("服务端请求复位连接,channel: {}", ctx.channel());
            TunnelUtil.closeGracefully(remoteChannel);
            ChannelPromise responsePromise = ctx.newPromise();
            responsePromise.addListener(f -> {
                if(f.isSuccess()) {
                    resetToCommandStatus(ctx);
                    PooledTunnelProvider.closeTunnel(ctx.channel());
                }
            });
            ctx.writeAndFlush(TunnelCommand.RESET_STATUS_TO_COMMAND_RESPONSE, responsePromise);
        } else if(TunnelCommand.RESET_STATUS_TO_COMMAND_RESPONSE == command) {
            logger.info("服务端已复位连接,channel: {}", ctx.channel());
            resetToCommandStatus(ctx);
            PooledTunnelProvider.closeTunnel(ctx.channel());
        } else {
            throw new TunnelException("不支持的命令: " + command);
        }
    }

    private void resetToCommandStatus(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        Socks5TunnelClientMessageHandler socks5TunnelClientMessageHandler = pipeline.remove(Socks5TunnelClientMessageHandler.class);
        pipeline.addBefore(NettyHandlerName.SOCKS5_TUNNEL_CLIENT_FORWARDING_MESSAGE_HANDLER, "socks5ProxyClientMessageHandler", socks5TunnelClientMessageHandler);
    }

    public Channel getRemoteChannel() {
        return remoteChannel;
    }

    public void setRemoteChannel(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    public Socks5TunnelClientCommandHandler(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }
}
