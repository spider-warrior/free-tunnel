package cn.t.freetunnel.common.constants;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * NettyAttrConstants
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-11 19:16
 **/
public class NettyAttrConstants {
    public static final AttributeKey<Boolean> CLOSE_BY_CALL_METHOD = AttributeKey.newInstance("closedByCallMethod");
    public static final AttributeKey<Channel> CONNECT_TUNNEL_REMOTE_CHANNEL = AttributeKey.newInstance("connectTunnelRemoteChannel");
}
