package cn.t.tool.netproxytool.socks5.client.handler;

import cn.t.tool.netproxytool.event.ProxyConnectionBuildResultListener;
import cn.t.tool.netproxytool.exception.ProxyException;
import cn.t.tool.netproxytool.handler.ForwardingMessageHandler;
import cn.t.tool.netproxytool.handler.LengthBasedEncryptedMessageDecoder;
import cn.t.tool.netproxytool.handler.LengthBasedPlainMessageEncoder;
import cn.t.tool.netproxytool.http.config.Socks5ClientConfig;
import cn.t.tool.netproxytool.http.constants.ProxyBuildExecutionStatus;
import cn.t.tool.netproxytool.socks5.constants.Socks5CmdExecutionStatus;
import cn.t.tool.netproxytool.socks5.constants.Socks5Method;
import cn.t.tool.netproxytool.socks5.constants.Socks5ProtocolConstants;
import cn.t.tool.netproxytool.socks5.constants.Socks5ServerState;
import cn.t.tool.netproxytool.socks5.server.handler.Socks5ServerMessageHandler;
import cn.t.tool.netproxytool.socks5.util.Socks5MessageUtil;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Socks5ClientMessageHandler
 * @author <a href="mailto:yangjian@ifenxi.com">研发部-杨建</a>
 * @version V1.0
 * @since 2021-08-27 16:09
 **/
public class Socks5ClientMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Socks5ServerMessageHandler.class);

    private final String targetHost;
    private final short targetPort;
    private final Socks5ClientConfig socks5ClientConfig;
    private final ProxyConnectionBuildResultListener proxyConnectionBuildResultListener;
    private final ChannelHandlerContext remoteChannelHandlerContext;

    private Socks5ServerState state = Socks5ServerState.NEGOTIATE;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf)msg;
        if(Socks5ServerState.NEGOTIATE == state) {
            //解析协商响应要素
            byte version = byteBuf.readByte();
            byte methodByte = byteBuf.readByte();
            Socks5Method socks5Method = Socks5Method.getSocks5Method(methodByte);
            if(socks5Method == null || Socks5Method.NO_ACCEPTABLE_METHODS == socks5Method) {
                throw new ProxyException("客户端未提供支持的认证方法");
            }
            //处理协商结果
            //不需要认证, 直接构建cmd请求
            if(Socks5Method.NO_AUTHENTICATION_REQUIRED == socks5Method) {
                ByteBuf outputBuf = Socks5MessageUtil.buildConnectBuf(targetHost.getBytes(), targetPort);
                ctx.writeAndFlush(outputBuf);
                state = Socks5ServerState.CMD;
                //用户名密码认证
            } else if(Socks5Method.USERNAME_PASSWORD == socks5Method) {
                if(socks5ClientConfig == null) {
                    throw new ProxyException("客户端未配置Socks5ClientConfig");
                }
                ByteBuf outputBuf = Socks5MessageUtil.buildUsernamePasswordAuthenticationBuf(socks5ClientConfig.getUsername().getBytes(), socks5ClientConfig.getPassword().getBytes());
                ctx.writeAndFlush(outputBuf);
                state = Socks5ServerState.AUTHENTICATE;
            } else {
                throw new ProxyException("客户端未实现的方法处理: " + socks5Method);
            }
        } else if(Socks5ServerState.AUTHENTICATE == state) {
            //解析鉴权响应要素
            byte version = byteBuf.readByte();
            byte status = byteBuf.readByte();
            logger.info("[{} -> {}]鉴权结果: version: {}, status: {}({})", ctx.channel().localAddress(), ctx.channel().remoteAddress(), version, status, Socks5CmdExecutionStatus.getSocks5CmdExecutionStatus(status));
            //处理鉴权结果
            if(Socks5CmdExecutionStatus.SUCCEEDED.value == status) {
                ByteBuf outputBuf = Socks5MessageUtil.buildConnectBuf(targetHost.getBytes(), targetPort);
                ctx.writeAndFlush(outputBuf);
                state = Socks5ServerState.CMD;
            } else {
                ctx.close();
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
            short targetPort = byteBuf.readShort();
            //处理命令响应
            if(Socks5CmdExecutionStatus.SUCCEEDED.value == status) {
                logger.info("[{} -> {}- > {} -> {}]: 连接成功, 回调监听器", remoteChannelHandlerContext.channel().remoteAddress(), remoteChannelHandlerContext.channel().localAddress(), ctx.channel().localAddress(), ctx.channel().remoteAddress());
                ChannelPipeline channelPipeline = ctx.channel().pipeline();
                //remove socks5 inbound handlers
                NettyComponentUtil.removeAllHandler(channelPipeline, Socks5ClientMessageHandler.class);
                if(socks5ClientConfig != null) {
                    //默认使用基于length-body结构解密数据
                    NettyComponentUtil.addLastHandler(channelPipeline, "length-based-encrypted-message-decoder", new LengthBasedEncryptedMessageDecoder(socks5ClientConfig.getSecurity()));
                    //默认使用基于length-body结构加密数据
                    NettyComponentUtil.addLastHandler(channelPipeline, "length-based-plain-message-encoder", new LengthBasedPlainMessageEncoder(socks5ClientConfig.getSecurity()));
                }
                NettyComponentUtil.addLastHandler(channelPipeline, "http-via-socks5-proxy-forwarding-handler", new ForwardingMessageHandler(remoteChannelHandlerContext));
                proxyConnectionBuildResultListener.handle(ProxyBuildExecutionStatus.SUCCEEDED.value, ctx);
            } else {
                logger.warn("连接代理服务器失败, status: {}", status);
            }
        } else {
            throw new ProxyException("未实现的状态处理: " + state);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //send negotiate msg
        ByteBuf outputBuf = ByteBufAllocator.DEFAULT.buffer(2);
        outputBuf.writeByte(Socks5ProtocolConstants.VERSION);
        outputBuf.writeByte(1);
        if(socks5ClientConfig == null) {
            outputBuf.writeByte(Socks5Method.NO_AUTHENTICATION_REQUIRED.rangeStart);
        } else {
            outputBuf.writeByte(Socks5Method.USERNAME_PASSWORD.rangeStart);
        }
        ctx.writeAndFlush(outputBuf);
    }

    public Socks5ClientMessageHandler(String targetHost, short targetPort, Socks5ClientConfig socks5ClientConfig, ProxyConnectionBuildResultListener proxyConnectionBuildResultListener, ChannelHandlerContext remoteChannelHandlerContext) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.socks5ClientConfig = socks5ClientConfig;
        this.proxyConnectionBuildResultListener = proxyConnectionBuildResultListener;
        this.remoteChannelHandlerContext = remoteChannelHandlerContext;
    }
}
