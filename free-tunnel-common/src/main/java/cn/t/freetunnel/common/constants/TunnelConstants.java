package cn.t.freetunnel.common.constants;

import cn.t.freetunnel.common.eventloppgroup.TunnelNioEventLoopGroup;
import io.netty.channel.EventLoopGroup;

public class TunnelConstants {
    public static final EventLoopGroup WORKER_GROUP = new TunnelNioEventLoopGroup(Runtime.getRuntime().availableProcessors());
}
