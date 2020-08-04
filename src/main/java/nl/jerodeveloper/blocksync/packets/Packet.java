package nl.jerodeveloper.blocksync.packets;

import lombok.Getter;

@Getter
public class Packet {

    private final PacketType packetType;
    private final PacketInfo packetInfo;

    public Packet(PacketType packetType, PacketInfo packetInfo) {
        this.packetInfo = packetInfo;
        this.packetType = packetType;
    }

    private transient String serialized = null;

    public String serialize() {
        if (serialized == null) {
            serialized = String.format("%s:%s", packetType.name(), packetInfo.serialize());
        }
        return serialized;
    }

}
