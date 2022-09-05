package cn.t.freetunnel.common.util;

import io.netty.buffer.ByteBuf;

public class ByteBufUtil {
    public static boolean enough(ByteBuf byteBuf, int length) {
        return byteBuf.readableBytes() >= length;
    }
}
