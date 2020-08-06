package nl.jerodeveloper.blocksync.interfaces;

import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.event.player.PlayerMoveEvent;

public interface PacketHelper {

    void sendMovementPacket(Redis redis, PlayerMoveEvent event);
    void sendLoginPacket(Redis redis, PlayerMoveEvent event);

}
