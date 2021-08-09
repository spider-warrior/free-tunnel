package cn.t.tool.netproxytool.socks5.client.analyse;

import cn.t.tool.netproxytool.socks5.model.AuthenticationResponse;
import cn.t.tool.nettytool.analyser.ByteBufAnalyser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AuthenticationResponse解析器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-14 21:05
 **/
public class AuthenticationResponseAnalyse extends ByteBufAnalyser {

    @Override
    public Object analyse(ByteBuf byteBuf, ChannelHandlerContext channelHandlerContext) {
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setVersion(byteBuf.readByte());
        authenticationResponse.setStatus(byteBuf.readByte());
        return authenticationResponse;
    }
}
