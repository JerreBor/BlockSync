package nl.jerodeveloper.blocksync.listeners.blocks;

import nl.jerodeveloper.blocksync.packets.Packet;
import nl.jerodeveloper.blocksync.packets.PacketInfo;
import nl.jerodeveloper.blocksync.packets.PacketType;
import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final Redis redis;

    public BlockPlaceListener(Redis redis) {
        this.redis = redis;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Location location = event.getBlock().getLocation();
        PacketInfo packetInfo = new PacketInfo(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName(), event.getBlock().getType().name());
        Packet packet = new Packet(PacketType.BLOCK_PLACE, packetInfo);
        redis.getTopic().publish(packet.serialize());
    }

}
