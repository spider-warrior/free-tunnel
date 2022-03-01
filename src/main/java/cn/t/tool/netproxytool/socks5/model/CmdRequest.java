package cn.t.tool.netproxytool.socks5.model;

import cn.t.tool.netproxytool.socks5.constants.Socks5AddressType;
import cn.t.tool.netproxytool.socks5.constants.Socks5Cmd;

import java.util.Arrays;

/**
 * 命令请求
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 20:13
 **/
public class CmdRequest {
    private byte version;
    private Socks5Cmd requestSocks5Cmd;
    private byte rsv;
    private Socks5AddressType socks5AddressType;
    private byte[] targetAddress;
    private int targetPort;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public Socks5Cmd getRequestSocks5Cmd() {
        return requestSocks5Cmd;
    }

    public void setRequestSocks5Cmd(Socks5Cmd requestSocks5Cmd) {
        this.requestSocks5Cmd = requestSocks5Cmd;
    }

    public byte getRsv() {
        return rsv;
    }

    public void setRsv(byte rsv) {
        this.rsv = rsv;
    }

    public Socks5AddressType getSocks5AddressType() {
        return socks5AddressType;
    }

    public void setSocks5AddressType(Socks5AddressType socks5AddressType) {
        this.socks5AddressType = socks5AddressType;
    }

    public byte[] getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(byte[] targetAddress) {
        this.targetAddress = targetAddress;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    @Override
    public String toString() {
        return "CmdRequest{" +
            "version=" + version +
            ", requestSocks5Cmd=" + requestSocks5Cmd +
            ", rsv=" + rsv +
            ", socks5AddressType=" + socks5AddressType +
            ", targetAddress=" + Arrays.toString(targetAddress) +
            ", targetPort=" + targetPort +
            '}';
    }
}
