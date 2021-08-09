package cn.t.tool.netproxytool.socks5.server.analyse;

import cn.t.tool.netproxytool.socks5.model.MethodRequest;
import cn.t.tool.nettytool.analyser.ByteBufAnalyser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 协商请求解析器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 20:53
 **/
public class MethodRequestAnalyse extends ByteBufAnalyser {

    @Override
    public Object analyse(ByteBuf byteBuf, ChannelHandlerContext channelHandlerContext) {
        if(byteBuf.readableBytes() < 2) {
            return null;
        }
        MethodRequest methodRequest = new MethodRequest();
        methodRequest.setVersion(byteBuf.readByte());
        byte methodLength = byteBuf.readByte();
        byte[] methodBytes = new byte[methodLength];
        byteBuf.readBytes(methodBytes);
        methodRequest.setMethods(methodBytes);
        return methodRequest;
    }

}
