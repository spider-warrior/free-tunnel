package cn.t.tool.netproxytool.socks5.constants;

/**
 * ι΄ζηΆζ
 */
public enum AuthenticationStatus {
    SUCCESS((byte)0),
    FAILED((byte)1)
    ;
    public final byte value;

    AuthenticationStatus(byte value) {
        this.value = value;
    }

    public static AuthenticationStatus getAuthenticationStatus(byte value) {
        for(AuthenticationStatus status: values()) {
            if(status.value == value) {
                return status;
            }
        }
        return null;
    }
}
