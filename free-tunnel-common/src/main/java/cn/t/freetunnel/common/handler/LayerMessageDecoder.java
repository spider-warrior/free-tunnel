package cn.t.freetunnel.common.handler;

import cn.t.freetunnel.common.constants.TunnelCommand;
import cn.t.freetunnel.common.constants.TunnelMessageType;
import cn.t.freetunnel.common.exception.TunnelException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * LayerMessageDecoder
 *
 * @author <a href="mailto:jian.yang@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-11 14:51
 **/
public class LayerMessageDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        byte messageTypeByte = in.readByte();
        TunnelMessageType messageType = TunnelMessageType.getTunnelMessageType(messageTypeByte);
        if(TunnelMessageType.DATA == messageType) {
            out.add(in.retainedDuplicate());
        } else if(TunnelMessageType.COMMAND == messageType) {
            out.add(TunnelCommand.getTunnelCommand(in.readByte()));
        } else {
            throw new TunnelException("不支持的消息类型: " + messageTypeByte);
        }
    }
}
