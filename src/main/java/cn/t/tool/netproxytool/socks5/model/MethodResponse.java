package cn.t.tool.netproxytool.socks5.model;

/**
 * 服务器协商响应
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 20:37
 **/
public class MethodResponse {
    private byte version;
    private byte socks5Method;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getSocks5Method() {
        return socks5Method;
    }

    public void setSocks5Method(byte socks5Method) {
        this.socks5Method = socks5Method;
    }

    @Override
    public String toString() {
        return "MethodResponse{" +
            "version=" + version +
            ", socks5Method=" + socks5Method +
            '}';
    }
}
