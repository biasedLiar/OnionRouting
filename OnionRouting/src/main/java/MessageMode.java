import java.util.HashMap;
import java.util.Map;

public enum MessageMode {
    //https://codingexplained.com/coding/java/enum-to-integer-and-integer-to-enum
    KEY_EXCHANGE((byte)1),
    FORWARD_ON_NETWORK((byte)2),
    FORWARD_OFF_NETWORK((byte)3);

    private byte value;
    private static Map map = new HashMap<>();

    private MessageMode(byte value){
        this.value = value;
    }

    static {
        for (MessageMode messageMode : MessageMode.values()){
            map.put(messageMode.value, messageMode);
        }
    }

    public static MessageMode valueOf(byte mode) {
        return (MessageMode) map.get(mode);
    }

    public byte getValue(){
        return value;
    }

}
