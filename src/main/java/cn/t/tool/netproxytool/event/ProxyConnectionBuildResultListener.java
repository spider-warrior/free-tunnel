package cn.t.tool.netproxytool.event;

import io.netty.channel.ChannelHandlerContext;

/**
 * 代理构建结果通知
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 21:48
 **/
@FunctionalInterface
public interface ProxyConnectionBuildResultListener {
    void handle(byte status, ChannelHandlerContext remoteChannelHandlerContext) throws Exception;
}
