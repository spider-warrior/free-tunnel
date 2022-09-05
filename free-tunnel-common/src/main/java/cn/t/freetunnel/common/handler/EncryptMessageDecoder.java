package cn.t.freetunnel.common.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class EncryptMessageDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(EncryptMessageDecoder.class);

    private final Cipher cipher;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IllegalBlockSizeException, BadPaddingException {
        if(in.readableBytes() < 4) {
            logger.debug("return immediately, readable bytes length less than 4");
            return;
        }
        in.markReaderIndex();
        //size
        int size = in.readInt();
        if(in.readableBytes() < size) {
            in.resetReaderIndex();
            logger.debug("return immediately, readable bytes length less than required: {}, actually: {}", size, in.readableBytes());
            return;
        }
        //content
        byte[] bytes = new byte[size];
        in.readBytes(bytes);
        bytes = cipher.doFinal(bytes);
        out.add(Unpooled.wrappedBuffer(bytes));
    }

    public EncryptMessageDecoder(byte[] keyBytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        this.cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
    }
}
