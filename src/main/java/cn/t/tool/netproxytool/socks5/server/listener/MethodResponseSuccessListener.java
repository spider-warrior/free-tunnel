package cn.t.tool.netproxytool.socks5.server.listener;

import cn.t.tool.netproxytool.exception.ProxyException;
import cn.t.tool.netproxytool.socks5.constants.Socks5Method;
import cn.t.tool.netproxytool.socks5.server.analyse.CmdRequestAnalyse;
import cn.t.tool.netproxytool.socks5.server.analyse.authenticationanalyse.UsernamePasswordAuthenticationAnalyse;
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
public class MethodResponseSuccessListener implements ChannelFutureListener {

    private static final Logger logger = LoggerFactory.getLogger(MethodResponseSuccessListener.class);

    private final NettyB2mDecoder nettyB2mDecoder;
    private final Socks5Method socks5Method;

    @Override
    public void operationComplete(ChannelFuture future) {
        if(future.isSuccess()) {
            if(Socks5Method.NO_AUTHENTICATION_REQUIRED == socks5Method) {
                nettyB2mDecoder.setByteBufAnalyser(new CmdRequestAnalyse());
            } else if(Socks5Method.USERNAME_PASSWORD == socks5Method) {
                nettyB2mDecoder.setByteBufAnalyser(new UsernamePasswordAuthenticationAnalyse());
            } else {
                throw new ProxyException("未实现的鉴权方法");
            }
        } else {
            logger.error("协商成功响应客户端失败", future.cause());
        }
    }

    public MethodResponseSuccessListener(NettyB2mDecoder nettyB2mDecoder, Socks5Method socks5Method) {
        this.nettyB2mDecoder = nettyB2mDecoder;
        this.socks5Method = socks5Method;
    }
}
