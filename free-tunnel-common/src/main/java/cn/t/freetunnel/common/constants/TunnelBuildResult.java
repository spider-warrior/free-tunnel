package cn.t.freetunnel.common.constants;

/**
 * 命令执行状态
 */
public enum TunnelBuildResult {
    SUCCEEDED((byte) 0X00),
    FAILED((byte) 0X01);

    public static TunnelBuildResult getTunnelBuildResult(byte value) {
        for(TunnelBuildResult status: values()) {
            if(status.value == value) {
                return status;
            }
        }
        return null;
    }

    public final byte value;
    TunnelBuildResult(byte value) {
        this.value = value;
    }
}
