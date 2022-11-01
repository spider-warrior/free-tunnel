package cn.t.freetunnel.common.constants;

/**
 * 内部命令
 */
public enum TunnelCommand {
    /**
     * 重置连接请求
     */
    RESET_REQUEST((byte)0),
    /**
     * 重置连接响应
     */
    RESET_RESPONSE((byte)1),
    /**
     * 重置连接到command步骤请求
     */
    RESET_STATUS_TO_COMMAND_REQUEST((byte)2),
    /**
     * 重置连接到command步骤响应
     */
    RESET_STATUS_TO_COMMAND_RESPONSE((byte)3),
    /**
     * 心跳
     */
    HEART_BEAT((byte)9)
    ;
    public final byte value;

    TunnelCommand(byte value) {
        this.value = value;
    }
    public static TunnelCommand getTunnelCommand(byte value) {
        for (TunnelCommand command : TunnelCommand.values()) {
            if(command.value == value) {
                return command;
            }
        }
        return null;
    }
}
