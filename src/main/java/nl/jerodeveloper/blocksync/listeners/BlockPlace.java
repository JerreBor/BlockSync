package nl.jerodeveloper.blocksync.listeners;

import jdk.nashorn.internal.ir.Block;
import nl.jerodeveloper.blocksync.packets.Packet;
import nl.jerodeveloper.blocksync.packets.PacketInfo;
import nl.jerodeveloper.blocksync.packets.PacketType;
import nl.jerodeveloper.blocksync.util.Redis;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlace implements Listener {

    private final Redis redis;

    public BlockPlace(Redis redis) {
        this.redis = redis;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Location location = event.getBlock().getLocation();
        PacketInfo packetInfo = new PacketInfo(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName(), event.getBlock().getType().name(), event.getBlock().getData());
        Packet packet = new Packet(PacketType.BLOCK_PLACE, packetInfo);

        redis.getPacketQueue().add(packet.serialize());
    }

}
