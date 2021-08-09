package cn.t.tool.netproxytool.socks5.client.analyse;

import cn.t.tool.netproxytool.socks5.constants.Socks5AddressType;
import cn.t.tool.netproxytool.socks5.model.CmdResponse;
import cn.t.tool.nettytool.analyser.ByteBufAnalyser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * CmdResponse解析器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-14 20:50
 **/
public class CmdResponseAnalyse extends ByteBufAnalyser {

    @Override
    public Object analyse(ByteBuf byteBuf, ChannelHandlerContext channelHandlerContext) {
        CmdResponse cmdResponse = new CmdResponse();
        //version
        cmdResponse.setVersion(byteBuf.readByte());
        //status
        cmdResponse.setExecutionStatus(byteBuf.readByte());
        //rsv
        cmdResponse.setRsv(byteBuf.readByte());
        //address type
        cmdResponse.setSocks5AddressType(byteBuf.readByte());
        //bind addr
        if(Socks5AddressType.IPV4.value == cmdResponse.getSocks5AddressType()) {
            byte[] address = new byte[4];
            byteBuf.readBytes(address);
            cmdResponse.setTargetAddress(address);
        } else if (Socks5AddressType.IPV6.value == cmdResponse.getSocks5AddressType()) {
            byte[] address = new byte[16];
            byteBuf.readBytes(address);
            cmdResponse.setTargetAddress(address);
        } else {
            byte[] address = new byte[byteBuf.readByte()];
            byteBuf.readBytes(address);
            cmdResponse.setTargetAddress(address);
        }
        //bind port
        cmdResponse.setTargetPort(byteBuf.readShort());
        return cmdResponse;
    }
}
