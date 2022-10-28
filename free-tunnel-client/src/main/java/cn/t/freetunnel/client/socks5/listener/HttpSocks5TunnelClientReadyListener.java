package cn.t.freetunnel.client.socks5.listener;

import cn.t.freetunnel.client.socks5.handler.HttpSocks5TunnelClientForwardingHandler;
import cn.t.freetunnel.client.socks5.handler.HttpSocks5TunnelClientHandler;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * http代理结果监听器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-27 15:42
 **/
public class HttpSocks5TunnelClientReadyListener extends Socks5TunnelClientReadyListener {

    @Override
    protected void notifySuccess(ChannelFuture future) {
        //已经通知客户端代理成功, 切换handler
        ChannelPipeline pipeline = localChannel.pipeline();
        pipeline.remove(HttpSocks5TunnelClientHandler.class);
        //http请求每次都需要修改header(connection), uri路径，所以不移除HttpRequestDecoder, HttpObjectAggregator
        pipeline.remove(HttpResponseEncoder.class);
        NettyComponentUtil.addLastHandler(pipeline, "http-socks5-client-forwarding-handler", new HttpSocks5TunnelClientForwardingHandler(remoteChannel));
    }

    public HttpSocks5TunnelClientReadyListener(Channel localChannel, Channel remoteChannel, String host, int port) {
        super(localChannel, remoteChannel, host, port);
    }
}
