package cn.t.tool.netproxytool.constants;

import io.netty.util.AttributeKey;

/**
 * socks5 trace metric
 *
 * @author <a href="mailto:yangjian@ifenxi.com">研发部-杨建</a>
 * @version V1.0
 * @since 2022-02-05 20:04
 **/
public class Socks5TraceConstants {
    public static final String SOCKS5_TRACE_LOG_NAME = "Socks5TraceLogger";
    public static final String SOCKS5_MESSAGE_CODEC_LOG_NAME = "MessageCodecLogger";

    //negotiate
    public static final AttributeKey<Long> NEGOTIATE_SEND_TIME_KEY = AttributeKey.newInstance("negotiateSendTime");
    public static final AttributeKey<Long> NEGOTIATE_RECEIVE_TIME_KEY = AttributeKey.newInstance("negotiateReceiveTime");
    //authentication
    public static final AttributeKey<Long> AUTHENTICATION_SEND_TIME_KEY = AttributeKey.newInstance("authenticationSendTime");
    public static final AttributeKey<Long> AUTHENTICATION_RECEIVE_TIME_KEY = AttributeKey.newInstance("authenticationReceiveTime");
    //command
    public static final AttributeKey<Long> COMMAND_SEND_TIME_KEY = AttributeKey.newInstance("commandSendTime");
    public static final AttributeKey<Long> COMMAND_RECEIVE_TIME_KEY = AttributeKey.newInstance("commandReceiveTime");
}
