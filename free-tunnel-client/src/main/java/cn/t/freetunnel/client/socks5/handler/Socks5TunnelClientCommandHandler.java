package cn.t.freetunnel.client.socks5.handler;

import cn.t.freetunnel.client.socks5.constants.ClientAttrConstants;
import cn.t.freetunnel.client.socks5.tunnelprovider.StaticChannelProvider;
import cn.t.freetunnel.common.constants.FreeTunnelConstants;
import cn.t.freetunnel.common.constants.NettyHandlerName;
import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.exception.TunnelException;
import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
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
            Attribute<Boolean> inUseAttr = ctx.channel().attr(ClientAttrConstants.TUNNEL_IN_USE);
            Boolean inUse = inUseAttr.get();
            if(Boolean.TRUE == inUse) {
                logger.info("服务端请求复位, channel: {}， 即将断开客户端连接: {}", ctx.channel(), remoteChannel);
                inUseAttr.set(null);
                TunnelUtil.closeGracefully(remoteChannel);
                ctx.writeAndFlush(TunnelCommand.RESET_STATUS_TO_COMMAND_RESPONSE);
                resetToCommandStatus(ctx);
                StaticChannelProvider.returnTunnel(ctx.channel());
            } else if(Boolean.FALSE == inUse) {
                logger.warn("客户端已发送复位请求, 忽略来自服务端的复位请求, channel: {}", ctx.channel());
            } else {
                logger.warn("通道不在使用中, 忽略来自服务端的复位请求, channel: {}", ctx.channel());
            }
        } else if(TunnelCommand.RESET_STATUS_TO_COMMAND_RESPONSE == command) {
            Attribute<Boolean> inUseAttr = ctx.channel().attr(ClientAttrConstants.TUNNEL_IN_USE);
            Boolean inUse = inUseAttr.get();
            if(Boolean.TRUE == inUse) {
                logger.warn("客户端未发送复位请求, 忽略来自服务端的复位响应, channel: {}", ctx.channel());
            } else if(Boolean.FALSE == inUse) {
                logger.info("来自服务端复位响应, 即将归还连接: {}", ctx.channel());
                inUseAttr.set(null);
                resetToCommandStatus(ctx);
                StaticChannelProvider.returnTunnel(ctx.channel());
            } else {
                logger.warn("通道不在使用中, 忽略自服务端的复位响应, channel: {}", ctx.channel());
            }
        } else {
            throw new TunnelException("不支持的命令: " + command);
        }
    }

    private void resetToCommandStatus(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        Socks5TunnelClientMessageHandler socks5TunnelClientMessageHandler = pipeline.remove(Socks5TunnelClientMessageHandler.class);
        pipeline.addBefore(NettyHandlerName.SOCKS5_TUNNEL_CLIENT_FORWARDING_MESSAGE_HANDLER, NettyHandlerName.SOCKS5_TUNNEL_CLIENT_MESSAGE_HANDLER, socks5TunnelClientMessageHandler);
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
