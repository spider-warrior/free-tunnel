package cn.t.freetunnel.client.socks5.listener;

import cn.t.freetunnel.client.socks5.handler.HttpSocks5TunnelClientForwardingHandler;
import cn.t.freetunnel.client.socks5.handler.HttpSocks5TunnelClientHandler;
import cn.t.freetunnel.common.listener.TunnelReadyListener;
import cn.t.freetunnel.common.util.TunnelUtil;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http代理结果监听器
 * 触发时机: 浏览器消息成功转发到服务端
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-27 15:42
 **/
public class HttpSocks5TunnelClientReadyListener extends TunnelReadyListener {

    private static final Logger logger = LoggerFactory.getLogger(HttpSocks5TunnelClientReadyListener.class);

    @Override
    protected void operationSuccess(ChannelFuture future) {
        //已经通知客户端代理成功, 切换handler
        ChannelPipeline pipeline = remoteChannel.pipeline();
        //http请求每次都需要修改header(connection), uri路径，所以不移除HttpRequestDecoder
        pipeline.remove(HttpResponseEncoder.class);
        pipeline.remove(HttpSocks5TunnelClientHandler.class);
        NettyComponentUtil.addLastHandler(pipeline, "http-socks5-client-forwarding-handler", new HttpSocks5TunnelClientForwardingHandler(future.channel()));
    }

    @Override
    protected void operationFailed(ChannelFuture future) {
        if(remoteChannel.isOpen()) {
            logger.error("[{}]: 消息转发失败, 即将关闭本地连接, 失败原因: {}", TunnelUtil.buildProxyTunnelName(remoteChannel, future.channel()), future.cause());
            TunnelUtil.closeImmediately(remoteChannel);
        } else {
            logger.error("[{}]: 消息转发失败, 本地连接已关闭, 失败原因: {}", TunnelUtil.buildProxyTunnelName(remoteChannel, future.channel()), future.cause());
        }
    }

    public HttpSocks5TunnelClientReadyListener(Channel remoteChannel, String host, int port) {
        super(remoteChannel, host, port);
    }
}
