package cn.t.tool.netproxytool.socks5.util;

import cn.t.tool.netproxytool.socks5.constants.Socks5AddressType;
import cn.t.tool.netproxytool.socks5.constants.Socks5Cmd;
import cn.t.tool.netproxytool.socks5.constants.Socks5ProtocolConstants;
import cn.t.tool.netproxytool.socks5.model.CmdRequest;
import cn.t.tool.netproxytool.socks5.model.UsernamePasswordAuthenticationRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * message构建工具
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-15 13:37
 **/
public class Socks5MessageUtil {

    public static CmdRequest buildConnectRequest(byte[] hostBytes, short port) {
        CmdRequest cmdRequest = new CmdRequest();
        cmdRequest.setVersion(Socks5ProtocolConstants.VERSION);
        cmdRequest.setRequestSocks5Cmd(Socks5Cmd.CONNECT);
        cmdRequest.setRsv((byte)0);
        cmdRequest.setSocks5AddressType(Socks5AddressType.DOMAIN);
        cmdRequest.setTargetAddress(hostBytes);
        cmdRequest.setTargetPort(port);
        return cmdRequest;
    }

    public static ByteBuf buildConnectBuf(byte[] hostBytes, short port) {
        ByteBuf outputBuf = ByteBufAllocator.DEFAULT.buffer(5 + hostBytes.length + 2);
        //version
        outputBuf.writeByte(Socks5ProtocolConstants.VERSION);
        //cmd
        outputBuf.writeByte(Socks5Cmd.CONNECT.value);
        //rsv
        outputBuf.writeByte((byte)0);
        //address type
        outputBuf.writeByte(Socks5AddressType.DOMAIN.value);
        //dst addr
        outputBuf.writeByte(hostBytes.length);
        outputBuf.writeBytes(hostBytes);
        //dst port
        outputBuf.writeShort(port);
        return outputBuf;
    }

    public static UsernamePasswordAuthenticationRequest buildUsernamePasswordAuthenticationRequest(byte[] username, byte[] password) {
        UsernamePasswordAuthenticationRequest usernamePasswordAuthenticationRequest = new UsernamePasswordAuthenticationRequest();
        usernamePasswordAuthenticationRequest.setVersion(Socks5ProtocolConstants.VERSION);
        usernamePasswordAuthenticationRequest.setUsername(username);
        usernamePasswordAuthenticationRequest.setPassword(password);
        return usernamePasswordAuthenticationRequest;
    }

    public static ByteBuf buildUsernamePasswordAuthenticationBuf(byte[] username, byte[] password) {
        ByteBuf outputBuf = ByteBufAllocator.DEFAULT.buffer();
        //version
        outputBuf.writeByte(Socks5ProtocolConstants.VERSION);
        //username length
        outputBuf.writeByte(username.length);
        //username
        outputBuf.writeBytes(username);
        //password length
        outputBuf.writeByte(password.length);
        //password
        outputBuf.writeBytes(password);
        return outputBuf;
    }

}
