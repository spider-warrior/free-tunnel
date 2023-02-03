package cn.t.freetunnel.server.http.encoder;

import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.util.ReferenceCountUtil;

public class ProxiedRequestEncoder extends MessageToByteEncoder<HttpObject> {

    private final HttpRequestEncoder httpRequestEncoder = new HttpRequestEncoder();
    private final EmbeddedChannel embeddedChannel = new EmbeddedChannel(httpRequestEncoder);

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpObject msg, ByteBuf out) {
        if(msg instanceof HttpRequest) {
            TunnelUtil.prepareProxiedRequest((HttpRequest)msg);
        } else if(msg instanceof ByteBufHolder) {
            ((ByteBufHolder)msg).retain();
        }
        embeddedChannel.writeOutbound(msg);
        ByteBuf byteBuf = embeddedChannel.readOutbound();
        //方案1
//        while (true) {
//            ByteBuf more = embeddedChannel.readOutbound();
//            if(more == null) {
//                break;
//            } else {
//                if(byteBuf instanceof CompositeByteBuf) {
//                    ((CompositeByteBuf)byteBuf).addComponent(true, more);
//                } else {
//                    byteBuf = Unpooled.wrappedBuffer(byteBuf, more);
//                }
//            }
//        }
        //方案2
//        CompositeByteBuf compositeBuffer = ctx.alloc().compositeBuffer();
//        while (true) {
//            ByteBuf byteBuf = embeddedChannel.readOutbound();
//            if(byteBuf != null) {
//                compositeBuffer.addComponent(true, byteBuf);
//            } else {
//                break;
//            }
//        }
        if(byteBuf.readableBytes() > 0) {
            try {
                out.writeBytes(byteBuf);
            } finally {
                ReferenceCountUtil.release(byteBuf);
            }
        }
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        embeddedChannel.close();
        super.close(ctx, promise);
    }
}
