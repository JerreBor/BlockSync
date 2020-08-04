package nl.jerodeveloper.blocksync.listeners;

import nl.jerodeveloper.blocksync.packets.Packet;
import nl.jerodeveloper.blocksync.packets.PacketInfo;
import nl.jerodeveloper.blocksync.packets.PacketType;
import nl.jerodeveloper.blocksync.util.Redis;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreak implements Listener {

    private final Redis redis;

    public BlockBreak(Redis redis) {
        this.redis = redis;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        PacketInfo packetInfo = new PacketInfo(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld().getName()
        );
        Packet packet = new Packet(PacketType.BLOCK_BREAK, packetInfo);

        redis.getPacketQueue().add(packet.serialize());
    }

}
