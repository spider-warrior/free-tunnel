package cn.t.tool.netproxytool.http.server.listener;

import cn.t.tool.netproxytool.handler.ForwardingMessageHandler;
import cn.t.tool.netproxytool.http.server.handler.HttpProxyServerViaSocks5Handler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https代理结果监听器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-27 15:42
 **/
public class HttpsProxyServerViaSocks5ClientConnectionReadyListener implements ChannelFutureListener {

    private static final Logger logger = LoggerFactory.getLogger(HttpsProxyServerViaSocks5ClientConnectionReadyListener.class);

    private final ChannelHandlerContext localChannelHandlerContext;
    private final ChannelHandlerContext remoteChannelHandlerContext;
    private final String clientName;

    @Override
    public void operationComplete(ChannelFuture future) {
        if(future.isSuccess()) {
            if(localChannelHandlerContext.channel().isOpen() && remoteChannelHandlerContext.channel().isOpen()) {
                logger.info("{}: 代理连接已就位", clientName);
                //已经通知客户端代理成功, 切换handler
                ChannelPipeline channelPipeline = localChannelHandlerContext.channel().pipeline();
                channelPipeline.remove(HttpRequestDecoder.class);
                channelPipeline.remove(HttpResponseEncoder.class);
                channelPipeline.remove(HttpObjectAggregator.class);
                channelPipeline.remove(HttpProxyServerViaSocks5Handler.class);
                channelPipeline.addLast("proxy-forwarding-handler", new ForwardingMessageHandler(remoteChannelHandlerContext));
            } else {
                logger.info("{}: 代理连接失败, 本地连接状态: {}, 远端连接状态: {}", clientName, localChannelHandlerContext.channel().isOpen(), remoteChannelHandlerContext.channel().isOpen());
            }
        } else {
            logger.error("{}: 代理连接失败, 即将关闭连接, 失败原因: {}", clientName, future.cause().getMessage());
            localChannelHandlerContext.close();
        }
    }

    public HttpsProxyServerViaSocks5ClientConnectionReadyListener(ChannelHandlerContext localChannelHandlerContext, ChannelHandlerContext remoteChannelHandlerContext, String clientName) {
        this.localChannelHandlerContext = localChannelHandlerContext;
        this.remoteChannelHandlerContext = remoteChannelHandlerContext;
        this.clientName = clientName;
    }
}
