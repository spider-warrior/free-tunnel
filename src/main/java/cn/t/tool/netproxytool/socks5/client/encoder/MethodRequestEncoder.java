package cn.t.tool.netproxytool.socks5.client.encoder;

import cn.t.tool.netproxytool.socks5.model.MethodRequest;
import cn.t.tool.nettytool.encoer.NettyM2bEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * MethodRequest编码器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-15 09:48
 **/
public class MethodRequestEncoder extends NettyM2bEncoder<MethodRequest> {

    @Override
    protected void doEncode(ChannelHandlerContext ctx, MethodRequest methodRequest, ByteBuf out) {
        //version
        out.writeByte(methodRequest.getVersion());
        //method length
        out.writeByte(methodRequest.getMethods().length);
        //methods
        out.writeBytes(methodRequest.getMethods());
    }
}
