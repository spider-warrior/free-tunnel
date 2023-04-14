package cn.t.freetunnel.client.socks5.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketUtil {

    private static final Map<String, Boolean> ipCache = new ConcurrentHashMap<>();
    public static boolean isSiteLocalAddress(String host) {
        if(ipCache.containsKey(host)) {
            return ipCache.get(host);
        }
        try {
            boolean isSiteLocalAddress = InetAddress.getByName(host).isSiteLocalAddress();
            ipCache.put(host, isSiteLocalAddress);
            return isSiteLocalAddress;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
