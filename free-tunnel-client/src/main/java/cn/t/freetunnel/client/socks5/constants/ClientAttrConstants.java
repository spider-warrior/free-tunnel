package cn.t.freetunnel.client.socks5.constants;

import cn.t.freetunnel.client.socks5.tunnelprovider.TunnelSpecification;
import io.netty.util.AttributeKey;

/**
 * NettyAttrConstants
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-11 19:16
 **/
public class ClientAttrConstants {
    public static final AttributeKey<TunnelSpecification> TUNNEL_SPECIFICATION = AttributeKey.newInstance("tunnelSpecification");
}
