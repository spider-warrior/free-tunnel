package cn.t.freetunnel.common.eventloop;

import cn.t.freetunnel.common.constants.NettyAttrConstants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutor;

public class TunnelNioEventLoopGroup extends NioEventLoopGroup {

    @Override
    public ChannelFuture register(Channel channel) {
        if(channel.hasAttr(NettyAttrConstants.EVENT_EXECUTOR)) {
            EventExecutor executor = channel.attr(NettyAttrConstants.EVENT_EXECUTOR).get();
            return ((EventLoop)executor).register(channel);
        } else {
            return super.register(channel);
        }
    }

    public TunnelNioEventLoopGroup(int nThreads) {
        super(nThreads);
    }
}
