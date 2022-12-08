package cn.t.freetunnel.client.socks5.listener;

import cn.t.freetunnel.client.socks5.handler.HttpSocks5TunnelClientForwardingHandler;
import cn.t.freetunnel.client.socks5.handler.HttpSocks5TunnelClientHandler;
import cn.t.freetunnel.client.socks5.util.Socks5MessageUtil;
import cn.t.freetunnel.common.listener.TunnelReadyListener;
import cn.t.freetunnel.common.util.TunnelUtil;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https代理结果监听器
 * 触发时机: 成功通知浏览器代理结果
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-27 15:42
 **/
public class HttpsSocks5TunnelClientReadyListener extends TunnelReadyListener {

    private static final Logger logger = LoggerFactory.getLogger(HttpsSocks5TunnelClientReadyListener.class);

    @Override
    protected void operationSuccess(ChannelFuture future) {
        //已经通知客户端代理成功, 切换handler
        ChannelPipeline channelPipeline = future.channel().pipeline();
        channelPipeline.remove(HttpSocks5TunnelClientHandler.class);
        channelPipeline.remove(HttpRequestDecoder.class);
        channelPipeline.remove(HttpResponseEncoder.class);
        channelPipeline.remove(HttpObjectAggregator.class);
        NettyComponentUtil.addLastHandler(channelPipeline, "https-socks5-client-forwarding-handler", new HttpSocks5TunnelClientForwardingHandler(remoteChannel));
    }

    @Override
    protected void operationFailed(ChannelFuture future) {
        if(remoteChannel.isOpen()) {
            logger.error("[{}]代理结果通知失败: {}:{}, 即将复位远程连接, 失败原因: {}", TunnelUtil.buildProxyTunnelName(future.channel(), remoteChannel), host, port, future.cause());
            Socks5MessageUtil.sendResetChannelRequest(remoteChannel);
        } else {
            logger.error("[{}]代理结果通知失败: {}:{}, 远程连接已关闭, 失败原因: {}", TunnelUtil.buildProxyTunnelName(future.channel(), remoteChannel), host, port, future.cause());
        }
    }

    public HttpsSocks5TunnelClientReadyListener(Channel remoteChannel, String host, int port) {
        super(remoteChannel, host, port);
    }
}
