package cn.t.tool.netproxytool.socks5.server.encoder;

import cn.t.tool.netproxytool.socks5.model.MethodResponse;
import cn.t.tool.nettytool.encoer.NettyM2bEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yj
 * @since 2020-01-12 16:27
 **/
@ChannelHandler.Sharable
public class MethodResponseEncoder extends NettyM2bEncoder<MethodResponse> {
    @Override
    protected void doEncode(ChannelHandlerContext ctx, MethodResponse methodResponse, ByteBuf out) {
        out.writeByte(methodResponse.getVersion());
        out.writeByte(methodResponse.getSocks5Method());
    }
}
