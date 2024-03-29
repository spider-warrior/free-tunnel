package cn.t.freetunnel.client.socks5.handler;

import cn.t.freetunnel.client.socks5.constants.ClientAttrConstants;
import cn.t.freetunnel.client.socks5.constants.Socks5TunnelClientConfig;
import cn.t.freetunnel.client.socks5.tunnelprovider.StaticChannelProvider;
import cn.t.freetunnel.client.socks5.tunnelprovider.TunnelSpecification;
import cn.t.freetunnel.client.socks5.util.Socks5MessageUtil;
import cn.t.freetunnel.common.constants.*;
import cn.t.freetunnel.common.exception.TunnelException;
import cn.t.freetunnel.common.handler.EncryptMessageDecoder;
import cn.t.freetunnel.common.handler.EncryptMessageEncoder;
import cn.t.freetunnel.common.handler.LayerMessageDecoder;
import cn.t.freetunnel.common.handler.LayerMessageEncoder;
import cn.t.freetunnel.server.http.encoder.ProxiedRequestEncoder;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Socks5ClientMessageHandler
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2021-08-27 16:09
 **/
@ChannelHandler.Sharable
public class Socks5TunnelClientMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(Socks5TunnelClientMessageHandler.class);

    private Socks5ServerState state = Socks5ServerState.NEGOTIATE;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        if(Socks5ServerState.NEGOTIATE == state) {
            //解析协商响应要素
            byte version = byteBuf.readByte();
            byte methodByte = byteBuf.readByte();
            Socks5Method socks5Method = Socks5Method.getSocks5Method(methodByte);
            if(socks5Method == null || Socks5Method.NO_ACCEPTABLE_METHODS == socks5Method) {
                throw new TunnelException("客户端未提供支持的认证方法");
            }
            //处理协商结果
            //不需要认证, 直接构建cmd请求
            if(Socks5Method.NO_AUTHENTICATION_REQUIRED == socks5Method) {
                TunnelSpecification tunnelSpecification = ctx.channel().attr(ClientAttrConstants.TUNNEL_SPECIFICATION).get();
                ByteBuf outputBuf = Socks5MessageUtil.buildConnectBuf(ctx.alloc(), tunnelSpecification.getTargetHost(), tunnelSpecification.getTargetPort());
                ctx.writeAndFlush(outputBuf);
                logger.info("no authentication, cmd request, channel: {}", ctx.channel());
                state = Socks5ServerState.CMD;
                //用户名密码认证
            } else if(Socks5Method.USERNAME_PASSWORD == socks5Method) {
                String username = Socks5TunnelClientConfig.username;
                String password = Socks5TunnelClientConfig.password;
                if(username == null || password == null) {
                    throw new TunnelException("客户端未配置用户名或密码, username: " + username + ", password: " + password);
                }
                ByteBuf outputBuf = Socks5MessageUtil.buildUsernamePasswordAuthenticationBuf(ctx.alloc(), username.getBytes(), password.getBytes());
                ctx.writeAndFlush(outputBuf);
                logger.info("authentication request, channel: {}", ctx.channel());
                state = Socks5ServerState.AUTHENTICATE;
            } else {
                throw new TunnelException("客户端未实现的方法处理: " + socks5Method);
            }
        } else if(Socks5ServerState.AUTHENTICATE == state) {
            //解析鉴权响应要素
            byte version = byteBuf.readByte();
            byte status = byteBuf.readByte();
            logger.info("[{} -> {}]鉴权结果: version: {}, status: {}({})", ctx.channel().localAddress(), ctx.channel().remoteAddress(), version, status, Socks5CmdExecutionStatus.getSocks5CmdExecutionStatus(status));
            //处理鉴权结果
            if(AuthenticationStatus.SUCCESS.value == status) {
                TunnelSpecification tunnelSpecification = ctx.channel().attr(ClientAttrConstants.TUNNEL_SPECIFICATION).get();
                ByteBuf outputBuf = Socks5MessageUtil.buildConnectBuf(ctx.alloc(), tunnelSpecification.getTargetHost(), tunnelSpecification.getTargetPort());
                ctx.writeAndFlush(outputBuf);
                logger.info("authenticate success, cmd request, channel: {}", ctx.channel());
                state = Socks5ServerState.CMD;
            } else {
                throw new TunnelException("鉴权失败: " + status);
            }
        } else if(Socks5ServerState.CMD == state) {
            //解析命令响应要素
            //version
            byte version = byteBuf.readByte();
            //status
            byte status = byteBuf.readByte();
            //rsv
            byte rsv = byteBuf.readByte();
            //address type
            byte addressType = byteBuf.readByte();
            //bind addr
            byte[] address = new byte[4];
            byteBuf.readBytes(address);
            //bind port
            int port = byteBuf.readChar();
            Channel remoteChannel = ctx.channel().attr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CHANNEL).get();
            logger.info("[{} -> {} -> {} -> {}]: cmd响应, version: {}, status: {}({}), addressType: {}({}), address: {}, port: {}", remoteChannel.remoteAddress(),
                remoteChannel.localAddress(),
                ctx.channel().localAddress(),
                ctx.channel().remoteAddress(),
                version,
                status,
                Socks5CmdExecutionStatus.getSocks5CmdExecutionStatus(status),
                addressType,
                Socks5AddressType.getAddressType(addressType),
                Arrays.toString(address),
                port);
            ChannelPipeline channelPipeline = ctx.channel().pipeline();
            Socks5TunnelClientForwardingHandler forwardingMessageHandler = (Socks5TunnelClientForwardingHandler)channelPipeline.get(NettyHandlerName.SOCKS5_TUNNEL_CLIENT_FORWARDING_MESSAGE_HANDLER);
            if(forwardingMessageHandler == null) {
                //proxied request encoder
                NettyComponentUtil.addFirst(channelPipeline, "proxiedRequestEncoder", new ProxiedRequestEncoder());
                //layer
                NettyComponentUtil.addFirst(channelPipeline, NettyHandlerName.LAYER_MESSAGE_DECODER, new LayerMessageDecoder());
                NettyComponentUtil.addFirst(channelPipeline, NettyHandlerName.LAYER_MESSAGE_ENCODER, new LayerMessageEncoder());
                //encrypt and decrypt
                byte[] security = Socks5TunnelClientConfig.security;
                NettyComponentUtil.addFirst(channelPipeline, NettyHandlerName.ENCRYPT_MESSAGE_DECODER, new EncryptMessageDecoder(security));
                NettyComponentUtil.addFirst(channelPipeline, NettyHandlerName.ENCRYPT_MESSAGE_ENCODER, new EncryptMessageEncoder(security));
                //forwarding
                NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.SOCKS5_TUNNEL_CLIENT_FORWARDING_MESSAGE_HANDLER, new Socks5TunnelClientForwardingHandler(remoteChannel));
                //command
                NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.SOCKS5_TUNNEL_CLIENT_COMMAND_HANDLER, new Socks5TunnelClientCommandHandler(remoteChannel));
            }
            //处理命令响应
            if(Socks5CmdExecutionStatus.SUCCEEDED.value == status) {
                if(forwardingMessageHandler != null) {
                    //forwardingHandler切换remoteContext
                    forwardingMessageHandler.setRemoteChannel(remoteChannel);
                    //commandHandler切换remoteContext
                    ((Socks5TunnelClientCommandHandler)channelPipeline.get(NettyHandlerName.SOCKS5_TUNNEL_CLIENT_COMMAND_HANDLER)).setRemoteChannel(remoteChannel);
                }
                //切换为forwardingHandler
                Socks5TunnelClientMessageHandler socks5TunnelClientMessageHandler = channelPipeline.remove(Socks5TunnelClientMessageHandler.class);
                NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.SOCKS5_TUNNEL_CLIENT_MESSAGE_HANDLER, socks5TunnelClientMessageHandler);
                ctx.channel().attr(ClientAttrConstants.TUNNEL_SPECIFICATION).get().getTunnelBuildResultListener().handle(TunnelBuildResult.SUCCEEDED.value, ctx.channel());
            } else {
                StaticChannelProvider.returnTunnel(ctx.channel());
                ctx.channel().attr(ClientAttrConstants.TUNNEL_SPECIFICATION).get().getTunnelBuildResultListener().handle(TunnelBuildResult.FAILED.value, null);
            }
        } else {
            throw new TunnelException("未实现的状态处理: " + state);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Attribute<Boolean> inUseAttr = ctx.channel().attr(ClientAttrConstants.TUNNEL_IN_USE);
        if(Boolean.TRUE.equals(inUseAttr.get())) {
            Channel remoteChannel = ctx.channel().attr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CHANNEL).get();
            if(remoteChannel.isOpen()) {
                //socks5通信过程中连接关闭惊醒进行listener回调(在与tunnel-server进行socks5协议交互时尚未切换到forwardingHandler时需要做额外的连接中断处理)
                logger.warn("[socks5-tunnel-client]与服务端连接断开，通知客户端代理失败，tunnelChannel: {}", ctx.channel());
                ctx.channel().attr(ClientAttrConstants.TUNNEL_SPECIFICATION).get().getTunnelBuildResultListener().handle(TunnelBuildResult.FAILED.value, null);
            } else {
                logger.warn("[socks5-tunnel-client]与服务端连接断开，客户端连接已关闭，不再进行通知，tunnelChannel: {}", ctx.channel());
            }
        } else {
            logger.warn("[socks5-tunnel-client]连接池连接与服务端断开，tunnelChannel: {}", ctx.channel());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //send negotiate msg
        ByteBuf outputBuf = ctx.alloc().buffer(3);
        outputBuf.writeByte(Socks5Constants.VERSION);
        outputBuf.writeByte(1);
        outputBuf.writeByte(Socks5Method.USERNAME_PASSWORD.value);
        ctx.writeAndFlush(outputBuf);
    }
}
