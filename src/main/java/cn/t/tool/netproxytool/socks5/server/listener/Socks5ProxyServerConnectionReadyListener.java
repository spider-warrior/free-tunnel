package cn.t.tool.netproxytool.socks5.server.listener;

import cn.t.tool.netproxytool.handler.ForwardingMessageHandler;
import cn.t.tool.netproxytool.handler.LengthBasedEncryptedMessageDecoder;
import cn.t.tool.netproxytool.handler.LengthBasedPlainMessageEncoder;
import cn.t.tool.netproxytool.socks5.server.handler.Socks5ServerMessageHandler;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * http代理结果监听器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-27 15:42
 **/
public class Socks5ProxyServerConnectionReadyListener implements ChannelFutureListener {

    private static final Logger logger = LoggerFactory.getLogger(Socks5ProxyServerConnectionReadyListener.class);

    private final ChannelHandlerContext localChannelHandlerContext;
    private final ChannelHandlerContext remoteChannelHandlerContext;
    private final String clientName;
    private final byte[] security;

    @Override
    public void operationComplete(ChannelFuture future) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        if(future.isSuccess()) {
            ChannelPipeline channelPipeline = localChannelHandlerContext.pipeline();
            //remove socks5 server message handlers
            NettyComponentUtil.removeAllHandler(channelPipeline, Socks5ServerMessageHandler.class);
            if(security != null) {
                //默认使用基于length-body结构解密数据
                NettyComponentUtil.addLastHandler(channelPipeline, "length-based-encrypted-message-decoder", new LengthBasedEncryptedMessageDecoder(security));
                //默认使用基于length-body结构加密数据
                NettyComponentUtil.addLastHandler(channelPipeline, "length-based-plain-message-encoder", new LengthBasedPlainMessageEncoder(security));
            }
            NettyComponentUtil.addLastHandler(channelPipeline, "socks5-proxy-forwarding-handler", new ForwardingMessageHandler(remoteChannelHandlerContext));
        } else {
            logger.error("{}: 代理请求发送失败, 即将关闭连接, 失败原因", clientName, future.cause());
            localChannelHandlerContext.close();
        }
    }

    public Socks5ProxyServerConnectionReadyListener(ChannelHandlerContext localChannelHandlerContext, ChannelHandlerContext remoteChannelHandlerContext, String clientName, byte[] security) {
        this.localChannelHandlerContext = localChannelHandlerContext;
        this.remoteChannelHandlerContext = remoteChannelHandlerContext;
        this.clientName = clientName;
        this.security = security;
    }
}
