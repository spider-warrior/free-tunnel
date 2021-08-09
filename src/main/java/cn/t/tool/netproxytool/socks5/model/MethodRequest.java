package cn.t.tool.netproxytool.socks5.model;

import java.util.Arrays;

/**
 * method请求
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-15 09:44
 **/
public class MethodRequest {
    private byte version;
    private byte[] methods;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte[] getMethods() {
        return methods;
    }

    public void setMethods(byte[] methods) {
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "MethodRequest{" +
            "version=" + version +
            ", methods=" + Arrays.toString(methods) +
            '}';
    }
}
