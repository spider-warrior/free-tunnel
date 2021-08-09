package cn.t.tool.netproxytool.socks5.constants;

import io.netty.handler.logging.LogLevel;

/**
 * 代理配置
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 23:00
 **/
public class Socks5ProxyConfig {

    public static final int SOCKS5_PROXY_READ_TIME_OUT_IN_SECONDS = 0;
    public static final int SOCKS5_PROXY_WRITE_TIME_OUT_IN_SECONDS = 0;
    public static final int SOCKS5_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS = 10;

    public static final LogLevel LOGGING_HANDLER_LOGGER_LEVEL = LogLevel.DEBUG;
}
