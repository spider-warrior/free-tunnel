package cn.t.freetunnel.common.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class EncryptMessageEncoder extends MessageToByteEncoder<ByteBuf> {

    private final Cipher cipher;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws IllegalBlockSizeException, BadPaddingException {
        if(msg.readableBytes() > 0) {
            byte[] bytes = new byte[msg.readableBytes()];
            msg.readBytes(bytes);
            byte[] encryptedBytes = cipher.doFinal(bytes);
            //length
            out.writeInt(encryptedBytes.length);
            //body
            out.writeBytes(encryptedBytes);
        }
    }

    public EncryptMessageEncoder(byte[] keyBytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        this.cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
    }
}
