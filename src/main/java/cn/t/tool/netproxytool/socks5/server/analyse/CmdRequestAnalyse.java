package cn.t.tool.netproxytool.socks5.server.analyse;

import cn.t.tool.netproxytool.exception.ProxyException;
import cn.t.tool.netproxytool.socks5.constants.Socks5AddressType;
import cn.t.tool.netproxytool.socks5.constants.Socks5Cmd;
import cn.t.tool.netproxytool.socks5.constants.Socks5ProtocolConstants;
import cn.t.tool.netproxytool.socks5.model.CmdRequest;
import cn.t.tool.nettytool.analyser.ByteBufAnalyser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 客户端命令请求解析器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 20:53
 **/
public class CmdRequestAnalyse extends ByteBufAnalyser {

    @Override
    public Object analyse(ByteBuf byteBuf, ChannelHandlerContext channelHandlerContext) {
        if(byteBuf.readableBytes() < 7) {
            return null;
        }
        //version
        byte version = byteBuf.readByte();
        if(version != Socks5ProtocolConstants.VERSION) {
            throw new ProxyException(String.format("不支持的协议版本: %d", version));
        }
        //cmd
        byte cmdByte = byteBuf.readByte();
        Socks5Cmd socks5Cmd = Socks5Cmd.getCmd(cmdByte);
        if(socks5Cmd == null) {
            throw new ProxyException(String.format("不支持的命令: %d", cmdByte));
        }
        //rsv
        byte rsv = byteBuf.readByte();
        if(rsv != 0) {
            throw new ProxyException(String.format("rsv必须为0, 实际传输值为: %d", rsv));
        }
        //address type
        byte addressTypeByte = byteBuf.readByte();
        Socks5AddressType socks5AddressType = Socks5AddressType.getAddressType(addressTypeByte);
        if(socks5AddressType == null) {
            throw new ProxyException(String.format("不支持的地址类型: %d", addressTypeByte));
        }
        //target address
        byte[] addressBytes = getAddressBytes(byteBuf, socks5AddressType);
        //target port
        short port = byteBuf.readShort();
        CmdRequest cmdRequest = new CmdRequest();
        cmdRequest.setVersion(version);
        cmdRequest.setRequestSocks5Cmd(socks5Cmd);
        cmdRequest.setRsv(rsv);
        cmdRequest.setSocks5AddressType(socks5AddressType);
        cmdRequest.setTargetAddress(addressBytes);
        cmdRequest.setTargetPort(port);
        return cmdRequest;
    }

    private byte[] getAddressBytes(ByteBuf byteBuf, Socks5AddressType socks5AddressType) {
        if(socks5AddressType == Socks5AddressType.IPV4) {
            byte[] bytes = new byte[4];
            byteBuf.readBytes(bytes);
            return bytes;
        } else if(socks5AddressType == Socks5AddressType.IPV6) {
            byte[] bytes = new byte[16];
            byteBuf.readBytes(bytes);
            return bytes;
        } else if(socks5AddressType == Socks5AddressType.DOMAIN) {
            byte[] bytes = new byte[byteBuf.readByte()];
            byteBuf.readBytes(bytes);
            return bytes;
        } else {
            throw new ProxyException("解析地址失败");
        }
    }
}
