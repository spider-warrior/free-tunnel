package cn.t.freetunnel.common.constants;

/**
 * CMD响应
 */
public enum Socks5CmdExecutionStatus {
    SUCCEEDED((byte) 0X00),
    GENERAL_SOCKS_SERVER_FAILURE((byte) 0X01),
    CONNECTION_NOT_ALLOWED_BY_RULESET((byte) 0X02),
    NETWORK_UNREACHABLE((byte) 0X03),
    HOST_UNREACHABLE((byte) 0X04),
    CONNECTION_REFUSED((byte) 0X05),
    TTL_EXPIRED((byte) 0X06),
    COMMAND_NOT_SUPPORTED((byte) 0X07),
    ADDRESS_TYPE_NOT_SUPPORTED((byte) 0X08),
    UNASSIGNED((byte) 0X09);

    public static Socks5CmdExecutionStatus getSocks5CmdExecutionStatus(byte value) {
        for(Socks5CmdExecutionStatus status: values()) {
            if(status.value == value) {
                return status;
            }
        }
        return UNASSIGNED;
    }

    public final byte value;

    Socks5CmdExecutionStatus(byte value) {
        this.value = value;
    }
}
