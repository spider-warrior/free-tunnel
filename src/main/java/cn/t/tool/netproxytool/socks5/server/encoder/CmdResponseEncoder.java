package cn.t.tool.netproxytool.socks5.server.encoder;

import cn.t.tool.netproxytool.exception.ProxyException;
import cn.t.tool.netproxytool.socks5.constants.Socks5AddressType;
import cn.t.tool.netproxytool.socks5.model.CmdResponse;
import cn.t.tool.nettytool.encoer.NettyM2bEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yj
 * @since 2020-01-12 16:27
 **/
@ChannelHandler.Sharable
public class CmdResponseEncoder extends NettyM2bEncoder<CmdResponse> {
    @Override
    protected void doEncode(ChannelHandlerContext ctx, CmdResponse cmdResponse, ByteBuf out) {
        out.writeByte(cmdResponse.getVersion());
        out.writeByte(cmdResponse.getExecutionStatus());
        out.writeByte(cmdResponse.getRsv());
        out.writeByte(cmdResponse.getSocks5AddressType());
        if(cmdResponse.getSocks5AddressType() == Socks5AddressType.IPV4.value) {
            out.writeBytes(cmdResponse.getTargetAddress());
        } else if(cmdResponse.getSocks5AddressType() == Socks5AddressType.IPV6.value) {
            out.writeBytes(cmdResponse.getTargetAddress());
        } else if(cmdResponse.getSocks5AddressType() == Socks5AddressType.DOMAIN.value) {
            out.writeByte(cmdResponse.getTargetAddress().length);
            out.writeBytes(cmdResponse.getTargetAddress());
        } else {
            throw new ProxyException(String.format("不支持的地址类型: %s", cmdResponse.getSocks5AddressType()));
        }
        out.writeShort(cmdResponse.getTargetPort());
    }
}
