package cn.t.freetunnel.common.constants;

public enum Socks5AddressType {

    IPV4((byte) 0X01),
    DOMAIN((byte) 0X03),
    IPV6((byte) 0X04);


    public final byte value;

    Socks5AddressType(byte value) {
        this.value = value;
    }

    public static Socks5AddressType getAddressType(byte value) {
        for(Socks5AddressType type: values()) {
            if(type.value == value) {
                return type;
            }
        }
        return null;
    }
}
