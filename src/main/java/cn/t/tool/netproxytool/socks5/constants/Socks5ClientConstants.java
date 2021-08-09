package cn.t.tool.netproxytool.socks5.constants;

import io.netty.util.AttributeKey;

/**
 * @author yj
 * @since 2020-05-26 20:24
 **/
public class Socks5ClientConstants {
    public static final byte[] SUPPORT_METHODS = {Socks5Method.NO_AUTHENTICATION_REQUIRED.rangeStart, Socks5Method.USERNAME_PASSWORD.rangeStart};
    public static final AttributeKey<String> TARGET_HOST_KEY = AttributeKey.newInstance("targetHost");
    public static final AttributeKey<Short> TARGET_PORT_KEY = AttributeKey.newInstance("targetPort");
    public static final AttributeKey<Boolean> USE_SAFE_ENCRYPT = AttributeKey.newInstance("useSageEncrypt");
}
