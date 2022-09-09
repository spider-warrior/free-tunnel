package cn.t.freetunnel.client.socks5.handler;

import cn.t.freetunnel.client.socks5.tunnelprovider.PooledTunnelProvider;
import cn.t.freetunnel.client.socks5.util.Socks5MessageUtil;
import cn.t.freetunnel.common.constants.*;
import cn.t.freetunnel.common.exception.TunnelException;
import cn.t.freetunnel.common.handler.EncryptMessageDecoder;
import cn.t.freetunnel.common.handler.EncryptMessageEncoder;
import cn.t.freetunnel.common.handler.LayerMessageDecoder;
import cn.t.freetunnel.common.handler.LayerMessageEncoder;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
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
                ByteBuf outputBuf = Socks5MessageUtil.buildConnectBuf(ctx.alloc(), ctx.channel().attr(NettyAttrConstants.CONNECT_TARGET_HOST).get(), ctx.channel().attr(NettyAttrConstants.CONNECT_TARGET_PORT).get());
                ctx.writeAndFlush(outputBuf);
                state = Socks5ServerState.CMD;
                //用户名密码认证
            } else if(Socks5Method.USERNAME_PASSWORD == socks5Method) {
                String username = ctx.channel().attr(NettyAttrConstants.CONNECT_USERNAME).get();
                String password = ctx.channel().attr(NettyAttrConstants.CONNECT_PASSWORD).get();
                if(username == null || password == null) {
                    throw new TunnelException("客户端未配置用户名或密码, username: " + username + ", password: " + password);
                }
                ByteBuf outputBuf = Socks5MessageUtil.buildUsernamePasswordAuthenticationBuf(ctx.alloc(), username.getBytes(), password.getBytes());
                ctx.writeAndFlush(outputBuf);
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
                ByteBuf outputBuf = Socks5MessageUtil.buildConnectBuf(ctx.alloc(), ctx.channel().attr(NettyAttrConstants.CONNECT_TARGET_HOST).get(), ctx.channel().attr(NettyAttrConstants.CONNECT_TARGET_PORT).get());
                ctx.writeAndFlush(outputBuf);
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
            ChannelHandlerContext remoteChannelHandlerContext = ctx.channel().attr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CONTEXT).get();
            logger.info("[{} -> {} -> {} -> {}]: cmd响应, version: {}, status: {}({}), addressType: {}({}), address: {}, port: {}", remoteChannelHandlerContext.channel().remoteAddress(),
                remoteChannelHandlerContext.channel().localAddress(),
                ctx.channel().localAddress(),
                ctx.channel().remoteAddress(),
                version,
                status,
                Socks5CmdExecutionStatus.getSocks5CmdExecutionStatus(status),
                addressType,
                Socks5AddressType.getAddressType(addressType),
                Arrays.toString(address),
                port);
            //处理命令响应
            if(Socks5CmdExecutionStatus.SUCCEEDED.value == status) {
                ChannelPipeline channelPipeline = ctx.channel().pipeline();
                Socks5TunnelClientForwardingHandler forwardingMessageHandler = (Socks5TunnelClientForwardingHandler)channelPipeline.get(NettyHandlerName.SOCKS5_TUNNEL_CLIENT_FORWARDING_MESSAGE_HANDLER);
                if(forwardingMessageHandler == null) {
                    //encrypt and decrypt
                    byte[] security = ctx.channel().attr(NettyAttrConstants.CONNECT_SECURITY).get();
                    NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.ENCRYPT_MESSAGE_DECODER, new EncryptMessageDecoder(security));
                    NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.ENCRYPT_MESSAGE_ENCODER, new EncryptMessageEncoder(security));
                    //layer
                    NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.LAYER_MESSAGE_DECODER, new LayerMessageDecoder());
                    NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.LAYER_MESSAGE_ENCODER, new LayerMessageEncoder());
                    //forwarding
                    NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.SOCKS5_TUNNEL_CLIENT_FORWARDING_MESSAGE_HANDLER, new Socks5TunnelClientForwardingHandler(remoteChannelHandlerContext));
                    //command
                    NettyComponentUtil.addLastHandler(channelPipeline, NettyHandlerName.SOCKS5_TUNNEL_CLIENT_COMMAND_HANDLER, new Socks5TunnelClientCommandHandler(remoteChannelHandlerContext));
                    //备份Socks5ProxyClientMessageHandler
                    Socks5TunnelClientMessageHandler socks5TunnelClientMessageHandler = channelPipeline.remove(Socks5TunnelClientMessageHandler.class);
                    NettyComponentUtil.addLastHandler(channelPipeline, "socks5ProxyClientMessageHandler", socks5TunnelClientMessageHandler);
                } else {
                    //forwardingHandler切换remoteContext
                    forwardingMessageHandler.setRemoteChannelHandlerContext(remoteChannelHandlerContext);
                    //commandHandler切换remoteContext
                    Socks5TunnelClientCommandHandler commandHandler = (Socks5TunnelClientCommandHandler)channelPipeline.get(NettyHandlerName.SOCKS5_TUNNEL_CLIENT_COMMAND_HANDLER);
                    commandHandler.setRemoteChannelHandlerContext(remoteChannelHandlerContext);
                    //备份Socks5ProxyClientMessageHandler
                    Socks5TunnelClientMessageHandler socks5TunnelClientMessageHandler = channelPipeline.remove(Socks5TunnelClientMessageHandler.class);
                    NettyComponentUtil.addLastHandler(channelPipeline, "socks5ProxyClientMessageHandler", socks5TunnelClientMessageHandler);
                }
                ctx.channel().attr(NettyAttrConstants.CONNECT_TUNNEL_BUILD_RESULT_LISTENER).get().handle(TunnelBuildResult.SUCCEEDED.value, ctx);
            } else {
                ctx.channel().attr(NettyAttrConstants.CONNECT_TUNNEL_BUILD_RESULT_LISTENER).get().handle(TunnelBuildResult.FAILED.value, null);
                PooledTunnelProvider.closeTunnel(ctx);
            }
        } else {
            throw new TunnelException("未实现的状态处理: " + state);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //send negotiate msg
        ByteBuf outputBuf = ctx.alloc().buffer(2);
        outputBuf.writeByte(Socks5Constants.VERSION);
        outputBuf.writeByte(1);
        if(ctx.channel().hasAttr(NettyAttrConstants.CONNECT_USERNAME) && ctx.channel().hasAttr(NettyAttrConstants.CONNECT_PASSWORD)) {
            outputBuf.writeByte(Socks5Method.USERNAME_PASSWORD.rangeStart);
        } else {
            outputBuf.writeByte(Socks5Method.NO_AUTHENTICATION_REQUIRED.rangeStart);
        }
        ctx.writeAndFlush(outputBuf);
    }
}
