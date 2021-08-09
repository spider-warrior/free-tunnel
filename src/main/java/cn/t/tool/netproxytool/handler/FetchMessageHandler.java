package cn.t.tool.netproxytool.handler;

import cn.t.tool.netproxytool.event.ProxyConnectionBuildResultListener;
import cn.t.tool.netproxytool.http.constants.ProxyBuildExecutionStatus;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * 抓取消息处理器处理器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 20:54
 **/
public class FetchMessageHandler extends ForwardingMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(FetchMessageHandler.class);

    private final ProxyConnectionBuildResultListener proxyConnectionBuildResultListener;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        proxyConnectionBuildResultListener.handle(ProxyBuildExecutionStatus.SUCCEEDED.value, ctx);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        ctx.connect(remoteAddress, localAddress, promise.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                //连接失败处理
                logger.info("[{}]: 连接失败, 回调监听器", ctx.channel().remoteAddress());
                proxyConnectionBuildResultListener.handle(ProxyBuildExecutionStatus.FAILED.value, ctx);
            }
        }));
    }

    public FetchMessageHandler(ChannelHandlerContext remoteChannelHandlerContext, ProxyConnectionBuildResultListener proxyConnectionBuildResultListener) {
        super(remoteChannelHandlerContext);
        this.proxyConnectionBuildResultListener = proxyConnectionBuildResultListener;
    }
}
