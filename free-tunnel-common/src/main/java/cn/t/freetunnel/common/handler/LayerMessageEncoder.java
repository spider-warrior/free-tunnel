package cn.t.freetunnel.common.handler;

import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.constants.TunnelMessageType;
import cn.t.freetunnel.common.exception.TunnelException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * LayerMessageEncoder
 *
 * @author <a href="mailto:jian.yang@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-11 14:51
 **/
public class LayerMessageEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        if(msg instanceof ByteBuf) {
            if(((ByteBuf)msg).readableBytes() > 0) {
                out.writeByte(TunnelMessageType.DATA.value);
                out.writeBytes((ByteBuf)msg);
            }
        } else if(msg instanceof TunnelCommand) {
            out.writeByte(TunnelMessageType.COMMAND.value);
            out.writeByte(((TunnelCommand)msg).value);
        } else {
            throw new TunnelException("不支持编码的消息类型: " + msg.getClass());
        }
    }
}
