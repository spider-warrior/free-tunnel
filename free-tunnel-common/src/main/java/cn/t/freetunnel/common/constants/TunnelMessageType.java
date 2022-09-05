package cn.t.freetunnel.common.constants;

/**
 * TunnelMessageType
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-10 16:19
 **/
public enum TunnelMessageType {
    DATA((byte)0),
    COMMAND((byte)1),
    ;
    public final byte value;

    public static TunnelMessageType getTunnelMessageType(byte value) {
        for (TunnelMessageType type : TunnelMessageType.values()) {
            if(type.value == value) {
                return type;
            }
        }
        return null;
    }

    TunnelMessageType(byte value) {
        this.value = value;
    }
}
