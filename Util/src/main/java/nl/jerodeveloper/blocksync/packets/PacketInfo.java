package nl.jerodeveloper.blocksync.packets;

import lombok.Getter;

public class PacketInfo {

    @Getter private final Object[] objects;

    public PacketInfo(Object... objects) {
        this.objects = objects;
    }

    private String serialized = null;

    public String serialize() {
        if (serialized == null) {
            String[] strings = new String[objects.length];

            for (int i = 0; i < objects.length; i++) {
                strings[i] = objects[i].toString();
            }

            serialized = String.join(";", strings);
        }
        return serialized;
    }

}
