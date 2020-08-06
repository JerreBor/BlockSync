package nl.jerodeveloper.blocksync.interfaces;

import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public interface PacketHelper {

    void sendMovementPacket(Redis redis, PlayerMoveEvent event);
    void sendLoginPacket(Redis redis, Player player, Location location);
    void sendLogoutPacket(Redis redis, Player player);
    void receiveLoginPacket(String[] packetInfoString);
    void receiveMoveLookPacket(String[] packetInfoString);
    void receiveMovePacket(String[] packetInfoString);
    void receiveLookPacket(String[] packetInfoString);
    void receiveLogoutPacket(String[] packetInfoString);

}
