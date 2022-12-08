package cn.t.freetunnel.server.http.listener;

import cn.t.freetunnel.common.handler.ForwardingMessageHandler;
import cn.t.freetunnel.common.listener.TunnelReadyListener;
import cn.t.tool.nettytool.util.NettyComponentUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * http代理结果监听器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-27 15:42
 **/
public class HttpTunnelReadyListener extends TunnelReadyListener {

    private final ChannelHandler channelHandler;

    @Override
    protected void operationSuccess(ChannelFuture future) {
        //已经通知客户端代理成功, 切换handler
        ChannelPipeline pipeline = remoteChannel.pipeline();
        pipeline.remove(HttpResponseEncoder.class);
        pipeline.remove(channelHandler);
        NettyComponentUtil.addLastHandler(pipeline, "proxy-forwarding-handler", new ForwardingMessageHandler(future.channel()));
    }

    public HttpTunnelReadyListener(Channel remoteChannel, String host, int port, ChannelHandler channelHandler) {
        super(remoteChannel, host, port);
        this.channelHandler = channelHandler;
    }
}
