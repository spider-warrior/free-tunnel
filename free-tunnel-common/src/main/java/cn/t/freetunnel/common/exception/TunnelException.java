package cn.t.freetunnel.common.exception;

/**
 * 通道异常
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 21:27
 **/
public class TunnelException extends RuntimeException {
    public TunnelException(String message) {
        super(message);
    }

    public TunnelException(Throwable cause) {
        super(cause);
    }
}
