package nl.jerodeveloper.blocksync.listeners.blocks;

import nl.jerodeveloper.blocksync.packets.Packet;
import nl.jerodeveloper.blocksync.packets.PacketInfo;
import nl.jerodeveloper.blocksync.packets.PacketType;
import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityChangeBlockListener implements Listener {

    private final Redis redis;

    public EntityChangeBlockListener(Redis redis) {
        this.redis = redis;
    }

    @EventHandler
    public void onChangeBlock(EntityChangeBlockEvent event) {
        Location location = event.getBlock().getLocation();
        PacketInfo packetInfo = new PacketInfo(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld().getName(),
                event.getTo().name()
        );

        Packet packet = new Packet(PacketType.BLOCK_PLACE, packetInfo);
        redis.getTopic().publish(packet.serialize());
    }

}
