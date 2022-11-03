package cn.t.freetunnel.client.socks5.tunnelprovider;

import cn.t.freetunnel.common.listener.TunnelBuildResultListener;

public class TunnelSpecification {
    private String targetHost;
    private int targetPort;
    private TunnelBuildResultListener tunnelBuildResultListener;

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public TunnelBuildResultListener getTunnelBuildResultListener() {
        return tunnelBuildResultListener;
    }

    public void setTunnelBuildResultListener(TunnelBuildResultListener tunnelBuildResultListener) {
        this.tunnelBuildResultListener = tunnelBuildResultListener;
    }

    public TunnelSpecification() {
    }

    public TunnelSpecification(String targetHost, int targetPort, TunnelBuildResultListener tunnelBuildResultListener) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.tunnelBuildResultListener = tunnelBuildResultListener;
    }

    @Override
    public String toString() {
        return "TunnelSpecification{" +
            "targetHost='" + targetHost + '\'' +
            ", targetPort=" + targetPort +
            ", tunnelBuildResultListener=" + tunnelBuildResultListener +
            '}';
    }
}
