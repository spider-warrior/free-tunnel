package cn.t.tool.netproxytool.socks5.client.listener;

import cn.t.tool.netproxytool.socks5.client.analyse.AuthenticationResponseAnalyse;
import cn.t.tool.netproxytool.socks5.server.handler.CmdRequestHandler;
import cn.t.tool.nettytool.decoder.NettyB2mDecoder;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 协商成功响应客户端监听器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-07 16:31
 **/
public class AuthenticationRequestWriteListener implements ChannelFutureListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationRequestWriteListener.class);

    private final NettyB2mDecoder nettyB2mDecoder;

    @Override
    public void operationComplete(ChannelFuture future) {
        if(future.isSuccess()) {
            nettyB2mDecoder.setByteBufAnalyser(new AuthenticationResponseAnalyse());
        } else {
            logger.error("鉴权请求失败", future.cause());
        }
    }

    public AuthenticationRequestWriteListener(NettyB2mDecoder nettyB2mDecoder) {
        this.nettyB2mDecoder = nettyB2mDecoder;
    }
}
