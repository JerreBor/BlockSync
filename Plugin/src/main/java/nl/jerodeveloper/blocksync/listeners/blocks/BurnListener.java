package nl.jerodeveloper.blocksync.listeners.blocks;

import nl.jerodeveloper.blocksync.packets.Packet;
import nl.jerodeveloper.blocksync.packets.PacketInfo;
import nl.jerodeveloper.blocksync.packets.PacketType;
import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;

public class BurnListener implements Listener {

    private final Redis redis;

    public BurnListener(Redis redis) {
        this.redis = redis;
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        Location location = event.getBlock().getLocation();
        PacketInfo packetInfo = new PacketInfo(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld().getName()
        );
        Packet packet = new Packet(PacketType.BLOCK_BREAK, packetInfo);

        redis.getTopic().publish(packet.serialize());
    }

}
