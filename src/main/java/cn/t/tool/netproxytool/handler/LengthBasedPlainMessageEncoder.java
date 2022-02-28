package cn.t.tool.netproxytool.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class LengthBasedPlainMessageEncoder extends MessageToByteEncoder<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(LengthBasedPlainMessageEncoder.class);

    private final Cipher cipher;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws IllegalBlockSizeException, BadPaddingException {
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        byte[] encryptedBytes = cipher.doFinal(bytes);
        //length
        out.writeInt(encryptedBytes.length + 8);
        out.writeLong(System.currentTimeMillis());
        //body
        out.writeBytes(encryptedBytes);
        logger.info("encrypt bytes: {} B", encryptedBytes.length);
    }

    public LengthBasedPlainMessageEncoder(byte[] keyBytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        this.cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
    }
}
