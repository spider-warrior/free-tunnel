package cn.t.freetunnel.client.socks5.util;

import cn.t.freetunnel.client.socks5.constants.ClientAttrConstants;
import cn.t.freetunnel.common.constants.Socks5AddressType;
import cn.t.freetunnel.common.constants.Socks5Cmd;
import cn.t.freetunnel.common.constants.Socks5Constants;
import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * message构建工具
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-15 13:37
 **/
public class Socks5MessageUtil {

    private static final Logger logger = LoggerFactory.getLogger(Socks5MessageUtil.class);

    public static ByteBuf buildConnectBuf(ByteBufAllocator allocator, String host, int port) {
        byte[] hostBytes = TunnelUtil.encryptHost(host).getBytes();
        ByteBuf outputBuf = allocator.buffer(5 + hostBytes.length + 2);
        //version
        outputBuf.writeByte(Socks5Constants.VERSION);
        //cmd
        outputBuf.writeByte(Socks5Cmd.CONNECT.value);
        //rsv
        outputBuf.writeByte((byte)10);
        //address type
        outputBuf.writeByte(Socks5AddressType.DOMAIN.value);
        //dst addr
        outputBuf.writeByte(hostBytes.length);
        outputBuf.writeBytes(hostBytes);
        //dst port
        outputBuf.writeShort(port);
        return outputBuf;
    }

    public static ByteBuf buildUsernamePasswordAuthenticationBuf(ByteBufAllocator allocator, byte[] username, byte[] password) {
        ByteBuf outputBuf = allocator.buffer();
        //version
        outputBuf.writeByte(Socks5Constants.VERSION);
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

    public static void sendResetChannelRequest(Channel channel) {
        Attribute<Boolean> inUseAttr = channel.attr(ClientAttrConstants.TUNNEL_IN_USE);
        Boolean inUse = inUseAttr.get();
        if(Boolean.TRUE == inUse) {
            logger.info("发送复位通道请求, channel: {}", channel);
            channel.writeAndFlush(TunnelCommand.RESET_STATUS_TO_COMMAND_REQUEST);
            inUseAttr.set(Boolean.FALSE);
        }
    }
}
