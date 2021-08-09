package cn.t.tool.netproxytool.http.constants;

/**
 * htt创建代理响应码
 */
public enum ProxyBuildExecutionStatus {
    SUCCEEDED((byte) 0X00),
    FAILED((byte) 0X01);

    public static ProxyBuildExecutionStatus getHttpProxyBuildExecutionStatus(byte value) {
        for(ProxyBuildExecutionStatus status: values()) {
            if(status.value == value) {
                return status;
            }
        }
        return null;
    }

    public final byte value;
    ProxyBuildExecutionStatus(byte value) {
        this.value = value;
    }
}
