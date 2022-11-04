package cn.t.freetunnel.server.http.encoder;

import cn.t.freetunnel.common.util.TunnelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;

public class ProxiedRequestEncoder extends MessageToByteEncoder<FullHttpRequest> {

    private final EmbeddedChannel embeddedChannel = new EmbeddedChannel(new HttpRequestEncoder());

    @Override
    protected void encode(ChannelHandlerContext ctx, FullHttpRequest msg, ByteBuf out) {
        TunnelUtil.prepareProxiedRequest(msg.retain());
        embeddedChannel.writeOutbound(msg.retain());
        ByteBuf byteBuf = embeddedChannel.readOutbound();
        out.writeBytes(byteBuf);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        embeddedChannel.close();
        super.close(ctx, promise);
    }
}
