package cn.t.tool.netproxytool.socks5.model;

/**
 * 用户名密码鉴权结果
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-14 20:08
 **/
public class AuthenticationResponse {
    private byte version;
    private byte status;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AuthenticationResponse{" +
            "version=" + version +
            ", status=" + status +
            '}';
    }
}
