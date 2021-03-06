package cn.t.tool.netproxytool.socks5.util;

import cn.t.tool.netproxytool.constants.Socks5TraceConstants;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Socks5TraceUtil
 *
 * @author <a href="mailto:yangjian@ifenxi.com">研发部-杨建</a>
 * @version V1.0
 * @since 2022-02-05 20:17
 **/
public class Socks5TraceUtil {
    private static final Logger socks5TraceLogger = LoggerFactory.getLogger(Socks5TraceConstants.SOCKS5_TRACE_LOG_NAME);
    private static final Logger socks5MessageCodecLogger = LoggerFactory.getLogger(Socks5TraceConstants.SOCKS5_MESSAGE_CODEC_LOG_NAME);

    public static void recordNegotiateSendTime(Channel channel) {
        channel.attr(Socks5TraceConstants.NEGOTIATE_SEND_TIME_KEY).set(System.currentTimeMillis());
    }
    public static void recordNegotiateReceiveTime(Channel channel) {
        channel.attr(Socks5TraceConstants.NEGOTIATE_RECEIVE_TIME_KEY).set(System.currentTimeMillis());
    }
    public static void recordAuthenticationSendTime(Channel channel) {
        channel.attr(Socks5TraceConstants.AUTHENTICATION_SEND_TIME_KEY).set(System.currentTimeMillis());
    }
    public static void recordAuthenticationReceiveTime(Channel channel) {
        channel.attr(Socks5TraceConstants.AUTHENTICATION_RECEIVE_TIME_KEY).set(System.currentTimeMillis());
    }
    public static void recordCommandSendTime(Channel channel) {
        channel.attr(Socks5TraceConstants.COMMAND_SEND_TIME_KEY).set(System.currentTimeMillis());
    }
    public static void recordCommandReceiveTime(Channel channel) {
        channel.attr(Socks5TraceConstants.COMMAND_RECEIVE_TIME_KEY).set(System.currentTimeMillis());
    }
    public static void connectionComplete(Channel remoteChannel, Channel localChannel, boolean success) {
        String status = success ? "成功" : "失败";
        Long negotiateSendTime = localChannel.attr(Socks5TraceConstants.NEGOTIATE_SEND_TIME_KEY).get();
        Long negotiateReceiveTime = localChannel.attr(Socks5TraceConstants.NEGOTIATE_RECEIVE_TIME_KEY).get();
        Long authenticationSendTime = localChannel.attr(Socks5TraceConstants.AUTHENTICATION_SEND_TIME_KEY).get();
        Long authenticationReceiveTime = localChannel.attr(Socks5TraceConstants.AUTHENTICATION_RECEIVE_TIME_KEY).get();
        Long commandSendTime = localChannel.attr(Socks5TraceConstants.COMMAND_SEND_TIME_KEY).get();
        Long commandReceiveTime = localChannel.attr(Socks5TraceConstants.COMMAND_RECEIVE_TIME_KEY).get();
        socks5TraceLogger.info("[{} -> {} -> {} -> {}]: 连接{}, detail:" +
            "\r\nnegotiateSendTime: {}" +
            "\r\nnegotiateReceiveTime: {}" +
            "\r\nauthenticationSendTime: {}" +
            "\r\nauthenticationReceiveTime: {}" +
            "\r\ncommandSendTime: {}" +
            "\r\ncommandReceiveTime: {}", remoteChannel.remoteAddress(), remoteChannel.localAddress(), localChannel.localAddress(), localChannel.remoteAddress(),
            status,
            negotiateSendTime,
            negotiateReceiveTime + "(" + (negotiateReceiveTime - negotiateSendTime) + ")",
            authenticationSendTime + "(" + (authenticationSendTime - negotiateReceiveTime) + ")",
            authenticationReceiveTime + "(" + (authenticationReceiveTime - authenticationSendTime) + ")",
            commandSendTime + "(" + (commandSendTime - authenticationReceiveTime) + ")",
            commandReceiveTime + "(" + (commandReceiveTime - commandSendTime) + ")"
        );
    }

    public static void decodeLog(long remoteTimestamp, long now, int size) {
        socks5MessageCodecLogger.info("收到消息, 延时: {}, 消息size: {}", now - remoteTimestamp, size);
    }
}
