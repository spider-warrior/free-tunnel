package cn.t.freetunnel.client.socks5.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SocketUtil {
    public static boolean isSiteLocalAddress(String host) {
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            return inetAddress.isSiteLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
