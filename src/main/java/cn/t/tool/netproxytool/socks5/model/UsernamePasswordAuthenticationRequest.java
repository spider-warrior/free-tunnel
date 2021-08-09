package cn.t.tool.netproxytool.socks5.model;

import java.util.Arrays;

/**
 * 用户名密码鉴权请求
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-14 19:33
 **/
public class UsernamePasswordAuthenticationRequest {
    private byte version;
    private byte[] username;
    private byte[] password;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte[] getUsername() {
        return username;
    }

    public void setUsername(byte[] username) {
        this.username = username;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UsernamePasswordAuthenticationRequest{" +
            "version=" + version +
            ", username=" + Arrays.toString(username) +
            ", password=" + Arrays.toString(password) +
            '}';
    }
}
