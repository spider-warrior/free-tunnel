package cn.t.freetunnel.server.socks5.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class Socks5MessageUtil {
    public static ByteBuf buildConnectResponse(ByteBufAllocator allocator, byte version, byte status, byte addressType, byte[] hostBytes, int port) {
        ByteBuf outputBuf = allocator.buffer(4 + hostBytes.length + 2);
        outputBuf.writeByte(version);
        outputBuf.writeByte(status);
        outputBuf.writeByte((byte)0);
        outputBuf.writeByte(addressType);
        outputBuf.writeBytes(hostBytes);
        outputBuf.writeShort(port);
        return outputBuf;
    }
}
