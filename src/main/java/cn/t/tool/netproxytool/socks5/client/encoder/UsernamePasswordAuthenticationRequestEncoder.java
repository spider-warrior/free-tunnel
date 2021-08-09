package cn.t.tool.netproxytool.socks5.client.encoder;

import cn.t.tool.netproxytool.socks5.model.UsernamePasswordAuthenticationRequest;
import cn.t.tool.nettytool.encoer.NettyM2bEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * NegotiateResponseHandler编码器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-14 19:43
 **/
public class UsernamePasswordAuthenticationRequestEncoder extends NettyM2bEncoder<UsernamePasswordAuthenticationRequest> {

    @Override
    protected void doEncode(ChannelHandlerContext ctx, UsernamePasswordAuthenticationRequest usernamePasswordAuthenticationRequest, ByteBuf out) {
        //version
        out.writeByte(usernamePasswordAuthenticationRequest.getVersion());
        //username length
        out.writeByte(usernamePasswordAuthenticationRequest.getUsername().length);
        //username
        out.writeBytes(usernamePasswordAuthenticationRequest.getUsername());
        //password length
        out.writeByte(usernamePasswordAuthenticationRequest.getPassword().length);
        //password
        out.writeBytes(usernamePasswordAuthenticationRequest.getPassword());
    }
}
