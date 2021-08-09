package cn.t.tool.netproxytool.socks5.client.handler;

import cn.t.tool.netproxytool.event.ProxyConnectionBuildResultListener;
import cn.t.tool.netproxytool.handler.ForwardingMessageHandler;
import cn.t.tool.netproxytool.handler.LengthBasedEncryptedMessageDecoder;
import cn.t.tool.netproxytool.handler.LengthBasedPlainMessageEncoder;
import cn.t.tool.netproxytool.http.config.Socks5ClientConfig;
import cn.t.tool.netproxytool.http.constants.ProxyBuildExecutionStatus;
import cn.t.tool.netproxytool.socks5.common.AbstractSocks5InboundHandler;
import cn.t.tool.netproxytool.socks5.constants.Socks5ClientConstants;
import cn.t.tool.netproxytool.socks5.constants.Socks5CmdExecutionStatus;
import cn.t.tool.netproxytool.socks5.model.CmdResponse;
import cn.t.tool.nettytool.decoder.NettyB2mDecoder;
import cn.t.tool.nettytool.encoer.NettyM2bEncoder;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * cmd响应处理器处理器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 22:30
 **/
public class CmdResponseHandler extends AbstractSocks5InboundHandler<CmdResponse> {

    private static final Logger logger = LoggerFactory.getLogger(CmdResponseHandler.class);

    private final ProxyConnectionBuildResultListener proxyConnectionBuildResultListener;
    private final ChannelHandlerContext remoteChannelHandlerContext;
    private final Socks5ClientConfig socks5ClientConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CmdResponse response) throws Exception {
        byte status = response.getExecutionStatus();
        if(Socks5CmdExecutionStatus.SUCCEEDED.value == status) {
            logger.info("[{} -> {}- > {} -> {}]: 连接成功, 回调监听器", remoteChannelHandlerContext.channel().remoteAddress(), remoteChannelHandlerContext.channel().localAddress(), ctx.channel().localAddress(), ctx.channel().remoteAddress());
            ChannelPipeline channelPipeline = ctx.channel().pipeline();
            //remove decoders
            NettyComponentUtil.removeAllHandler(channelPipeline, NettyB2mDecoder.class);
            //remove encoders
            NettyComponentUtil.removeAllHandler(channelPipeline, NettyM2bEncoder.class);
            //remove socks5 inbound handlers
            NettyComponentUtil.removeAllHandler(channelPipeline, AbstractSocks5InboundHandler.class);
            Boolean useSageEncrypt = ctx.channel().attr(Socks5ClientConstants.USE_SAFE_ENCRYPT).get();
            if(Boolean.TRUE == useSageEncrypt) {
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
    }

    public CmdResponseHandler(ProxyConnectionBuildResultListener proxyConnectionBuildResultListener, ChannelHandlerContext remoteChannelHandlerContext, Socks5ClientConfig socks5ClientConfig) {
        this.proxyConnectionBuildResultListener = proxyConnectionBuildResultListener;
        this.remoteChannelHandlerContext = remoteChannelHandlerContext;
        this.socks5ClientConfig = socks5ClientConfig;
    }
}
