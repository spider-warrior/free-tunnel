package cn.t.tool.netproxytool.http.config;

import java.util.Arrays;

/**
 * @author yj
 * @since 2020-05-26 20:08
 **/
public class Socks5ClientConfig {
    private String socks5ServerHost;
    private int socks5ServerPort;

    private String username;
    private String password;
    private byte[] security;

    public String getSocks5ServerHost() {
        return socks5ServerHost;
    }

    public void setSocks5ServerHost(String socks5ServerHost) {
        this.socks5ServerHost = socks5ServerHost;
    }

    public int getSocks5ServerPort() {
        return socks5ServerPort;
    }

    public void setSocks5ServerPort(int socks5ServerPort) {
        this.socks5ServerPort = socks5ServerPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getSecurity() {
        return security;
    }

    public void setSecurity(byte[] security) {
        this.security = security;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Socks5ClientConfig{");
        sb.append("socks5ServerHost='").append(socks5ServerHost).append('\'');
        sb.append(", socks5ServerPort=").append(socks5ServerPort);
        sb.append(", username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", security=").append(Arrays.toString(security));
        sb.append('}');
        return sb.toString();
    }
}
