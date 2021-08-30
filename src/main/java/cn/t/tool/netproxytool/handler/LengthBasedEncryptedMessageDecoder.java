package cn.t.tool.netproxytool.handler;

import cn.t.tool.nettytool.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
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

public class LengthBasedEncryptedMessageDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(LengthBasedEncryptedMessageDecoder.class);

    private final Cipher cipher;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IllegalBlockSizeException, BadPaddingException {
        if(in.readableBytes() < 4) {
            logger.info("return immediately, readable bytes length less than 4");
            return;
        }
        in.markReaderIndex();
        int size = in.readInt();
        if(in.readableBytes() < size) {
            in.resetReaderIndex();
            logger.info("return immediately, readable bytes length less than required: {}, actually: {}", size, in.readableBytes());
            return;
        }
        byte[] bytes = new byte[size];
        in.readBytes(bytes);
        bytes = cipher.doFinal(bytes);
        logger.info("解密消息: {} B", size);
        out.add(ByteBufUtil.createByteBuf(bytes));
    }

    public LengthBasedEncryptedMessageDecoder(byte[] keyBytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        this.cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
    }
}
