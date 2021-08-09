package cn.t.tool.netproxytool.socks5.server.analyse.authenticationanalyse;

import cn.t.tool.netproxytool.socks5.model.UsernamePasswordAuthenticationRequest;
import cn.t.tool.nettytool.analyser.ByteBufAnalyser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 用户名密码解析器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 22:05
 **/
public class UsernamePasswordAuthenticationAnalyse extends ByteBufAnalyser {

    @Override
    public Object analyse(ByteBuf byteBuf, ChannelHandlerContext channelHandlerContext) {
        UsernamePasswordAuthenticationRequest usernamePasswordAuthenticationRequest = new UsernamePasswordAuthenticationRequest();
        usernamePasswordAuthenticationRequest.setVersion(byteBuf.readByte());
        byte usernameLength = byteBuf.readByte();
        byte[] username = new byte[usernameLength];
        byteBuf.readBytes(username);
        usernamePasswordAuthenticationRequest.setUsername(username);
        byte passwordLength = byteBuf.readByte();
        byte[] password = new byte[passwordLength];
        byteBuf.readBytes(password);
        usernamePasswordAuthenticationRequest.setPassword(password);
        return usernamePasswordAuthenticationRequest;
    }
}
