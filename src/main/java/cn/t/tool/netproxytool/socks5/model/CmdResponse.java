package cn.t.tool.netproxytool.socks5.model;

import java.util.Arrays;

/**
 * 命令响应
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-21 00:35
 **/
public class CmdResponse {
    private byte version;
    private byte executionStatus;
    private byte rsv;
    private byte socks5AddressType;
    private byte[] targetAddress;
    private short targetPort;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(byte executionStatus) {
        this.executionStatus = executionStatus;
    }

    public byte getRsv() {
        return rsv;
    }

    public void setRsv(byte rsv) {
        this.rsv = rsv;
    }

    public byte getSocks5AddressType() {
        return socks5AddressType;
    }

    public void setSocks5AddressType(byte socks5AddressType) {
        this.socks5AddressType = socks5AddressType;
    }

    public byte[] getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(byte[] targetAddress) {
        this.targetAddress = targetAddress;
    }

    public short getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(short targetPort) {
        this.targetPort = targetPort;
    }

    @Override
    public String toString() {
        return "CmdResponse{" +
            "version=" + version +
            ", executionStatus=" + executionStatus +
            ", rsv=" + rsv +
            ", socks5AddressType=" + socks5AddressType +
            ", targetAddress=" + Arrays.toString(targetAddress) +
            ", targetPort=" + targetPort +
            '}';
    }
}
