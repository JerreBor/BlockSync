package nl.jerodeveloper.blocksync.v1_16_R1;

import nl.jerodeveloper.blocksync.interfaces.PacketHelper;
import nl.jerodeveloper.blocksync.packets.Packet;
import nl.jerodeveloper.blocksync.packets.PacketInfo;
import nl.jerodeveloper.blocksync.packets.PacketType;
import nl.jerodeveloper.blocksync.redis.Redis;

public class PacketHelperImpl implements PacketHelper {

    @Override
    public void sendMovementPacket(Redis redis, org.bukkit.event.player.PlayerMoveEvent event) {
        PacketInfo packetInfo = new PacketInfo();
        redis.getTopic().publish(new Packet(PacketType.PLAYER_MOVE, packetInfo));
    }

    @Override
    public void sendLoginPacket(Redis redis, org.bukkit.event.player.PlayerMoveEvent event) {
        PacketInfo packetInfo = new PacketInfo();
        redis.getTopic().publish(new Packet(PacketType.PLAYER_SPAWN, packetInfo));
    }
}
