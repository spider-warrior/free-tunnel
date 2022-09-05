package cn.t.freetunnel.common.constants;

import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;

/**
 * NettyAttrConstants
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-11 19:16
 **/
public class NettyAttrConstants {
    public static final AttributeKey<Boolean> CLOSE_BY_REMOTE = AttributeKey.newInstance("closedByRemote");
    public static final AttributeKey<EventExecutor> EVENT_EXECUTOR = AttributeKey.newInstance("eventExecutor");
    public static final AttributeKey<String> CONNECT_USERNAME = AttributeKey.newInstance("connectUsername");
    public static final AttributeKey<String> CONNECT_PASSWORD = AttributeKey.newInstance("connectPassword");
    public static final AttributeKey<String> CONNECT_TARGET_HOST = AttributeKey.newInstance("connectTargetHost");
    public static final AttributeKey<Integer> CONNECT_TARGET_PORT = AttributeKey.newInstance("connectTargetPort");
    public static final AttributeKey<byte[]> CONNECT_SECURITY = AttributeKey.newInstance("connectSecurity");
    public static final AttributeKey<ChannelHandlerContext> CONNECT_TUNNEL_REMOTE_CONTEXT = AttributeKey.newInstance("connectTunnelRemoteContext");
    public static final AttributeKey<TunnelBuildResultListener> CONNECT_TUNNEL_BUILD_RESULT_LISTENER = AttributeKey.newInstance("connectTunnelBuildResultListener");
}
