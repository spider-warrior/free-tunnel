package cn.t.tool.netproxytool.exception;

/**
 * 代理异常
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 21:27
 **/
public class ProxyException extends RuntimeException {
    public ProxyException(String message) {
        super(message);
    }
}
