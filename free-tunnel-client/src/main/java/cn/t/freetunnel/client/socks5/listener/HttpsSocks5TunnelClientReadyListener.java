package cn.t.freetunnel.client.socks5.listener;

import cn.t.freetunnel.client.socks5.handler.HttpSocks5TunnelClientHandler;
import cn.t.freetunnel.client.socks5.handler.HttpSocks5TunnelClientForwardingHandler;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * https代理结果监听器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-27 15:42
 **/
public class HttpsSocks5TunnelClientReadyListener extends Socks5TunnelClientReadyListener {

    @Override
    protected void notifySuccess(ChannelFuture future) {
        //已经通知客户端代理成功, 切换handler
        ChannelPipeline channelPipeline = localChannelHandlerContext.channel().pipeline();
        channelPipeline.remove(HttpSocks5TunnelClientHandler.class);
        channelPipeline.remove(HttpRequestDecoder.class);
        channelPipeline.remove(HttpResponseEncoder.class);
        channelPipeline.remove(HttpObjectAggregator.class);
        NettyComponentUtil.addLastHandler(channelPipeline, "https-socks5-client-forwarding-handler", new HttpSocks5TunnelClientForwardingHandler(remoteChannelHandlerContext));
    }

    public HttpsSocks5TunnelClientReadyListener(ChannelHandlerContext localChannelHandlerContext, ChannelHandlerContext remoteChannelHandlerContext, String host, int port) {
        super(localChannelHandlerContext, remoteChannelHandlerContext, host, port);
    }
}
