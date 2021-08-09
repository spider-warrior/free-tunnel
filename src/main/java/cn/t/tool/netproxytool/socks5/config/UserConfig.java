package cn.t.tool.netproxytool.socks5.config;

import java.util.Arrays;

/**
 * @author yj
 * @since 2020-05-26 20:07
 **/
public class UserConfig {
    private String username;
    private String password;
    private byte[] security;

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
        final StringBuilder sb = new StringBuilder("UserConfig{");
        sb.append("username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", security=").append(Arrays.toString(security));
        sb.append('}');
        return sb.toString();
    }
}
