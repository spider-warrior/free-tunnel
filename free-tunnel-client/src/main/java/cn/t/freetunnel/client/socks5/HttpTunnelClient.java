package cn.t.freetunnel.client.socks5;

import cn.t.freetunnel.client.socks5.constants.HttpTunnelClientConfig;
import cn.t.freetunnel.client.socks5.util.InitializerBuilder;
import cn.t.freetunnel.client.socks5.util.Socks5TunnelClientConfigUtil;
import cn.t.freetunnel.common.constants.TunnelConstants;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.server.NettyTcpServer;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import cn.t.tool.nettytool.launcher.DefaultLauncher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * http代理服务器客户端
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-24 11:05
 **/
public class HttpTunnelClient {
    public static void main(String[] args) throws IOException {
        Socks5TunnelClientConfigUtil.initClientConfig(args);
        NettyTcpChannelInitializer nettyChannelInitializer = InitializerBuilder.httpSocks5TunnelClientInitializer();
        NettyTcpServer proxyServer = new NettyTcpServer(
            "http-tunnel-client",
            new int[]{HttpTunnelClientConfig.SERVER_PORT},
            nettyChannelInitializer,
            TunnelConstants.BOSS_GROUP,
            TunnelConstants.WORKER_GROUP,
            false,
            true);
        List<DaemonService> daemonServerList = new ArrayList<>();
        daemonServerList.add(proxyServer);
        DefaultLauncher defaultLauncher = new DefaultLauncher();
        defaultLauncher.setDaemonServiceList(daemonServerList);
        defaultLauncher.startup();
    }
}
