package cn.t.freetunnel.server.socks5;

import cn.t.util.common.StringUtil;
import cn.t.util.common.SystemUtil;
import cn.t.util.common.digital.ByteUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务配置
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 23:00
 **/
public class TunnelServerConfig {

    public static final String PUBLIC_HOSTNAME = SystemUtil.getLocalIpV4(false);
    public static final String SERVER_HOST = StringUtil.isEmpty(PUBLIC_HOSTNAME) ? SystemUtil.getLocalIpV4(true) : PUBLIC_HOSTNAME;
    public static final byte[] SERVER_HOST_BYTES = ByteUtil.stringsToBytes(SERVER_HOST, "\\.");
    public static final int SERVER_PORT = 11186;

    public static final int SOCKS5_PROXY_READ_TIME_OUT_IN_SECONDS = 0;
    public static final int SOCKS5_PROXY_WRITE_TIME_OUT_IN_SECONDS = 0;
    public static final int SOCKS5_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS = 300;

    public static final String TUNNEL_SERVER_HOME_KEY = "TUNNEL_SERVER_HOME";
    public static final String TUNNEL_SERVER_USERS_CONFIG_FILE = "users";

    /**
     * 用户配置
     */
    public static Map<String, UserConfig> USER_CONFIG_MAP = new HashMap<>();

    public static class UserConfig {
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
}
