package cn.t.freetunnel.common.eventloop;

import cn.t.freetunnel.common.constants.NettyAttrConstants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;

public class TunnelNioEventLoopGroup extends NioEventLoopGroup {

    @Override
    public ChannelFuture register(Channel channel) {
        Channel localChannel = channel.attr(NettyAttrConstants.CONNECT_TUNNEL_REMOTE_CHANNEL).get();
        if(localChannel == null) {
            return super.register(channel);
        } else {
            return localChannel.eventLoop().register(channel);
        }
    }

    public TunnelNioEventLoopGroup(int nThreads) {
        super(nThreads);
    }
}
