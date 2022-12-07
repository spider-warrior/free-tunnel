package cn.t.freetunnel.common.constants;

import cn.t.freetunnel.common.eventloop.TunnelNioEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

public class TunnelConstants {
    public static final EventLoopGroup BOSS_GROUP = new TunnelNioEventLoopGroup(1, new DefaultThreadFactory("NettyServerBoss", true));
    public static final EventLoopGroup WORKER_GROUP = new TunnelNioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new DefaultThreadFactory("NettyServerWorker", true));
}
