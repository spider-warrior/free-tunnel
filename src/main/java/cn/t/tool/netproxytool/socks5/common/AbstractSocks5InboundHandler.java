package cn.t.tool.netproxytool.socks5.common;

import cn.t.tool.nettytool.aware.NettyB2mDecoderAware;
import cn.t.tool.nettytool.decoder.NettyB2mDecoder;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author <a href="mailto:yangjian@ifenxi.com">研发部-杨建</a>
 * @version V1.0
 * @since 2021-07-15 10:45
 **/
public abstract class AbstractSocks5InboundHandler<I> extends SimpleChannelInboundHandler<I> implements NettyB2mDecoderAware {
    protected NettyB2mDecoder nettyB2mDecoder;

    public NettyB2mDecoder getNettyB2mDecoder() {
        return nettyB2mDecoder;
    }

    @Override
    public void setNettyB2mDecoder(NettyB2mDecoder nettyB2mDecoder) {
        this.nettyB2mDecoder = nettyB2mDecoder;
    }
}
