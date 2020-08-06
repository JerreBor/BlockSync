package nl.jerodeveloper.blocksync.listeners;

import nl.jerodeveloper.blocksync.BlockSyncPlugin;
import nl.jerodeveloper.blocksync.packets.PacketType;
import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.redisson.api.RTopic;

public class PacketListener {

    private final Redis redis;
    private final BlockSyncPlugin plugin;

    public PacketListener(Redis redis, BlockSyncPlugin plugin) {
        this.redis = redis;
        this.plugin = plugin;
    }

    public void listen() {
        System.out.println("Listening for incoming packets...");
        RTopic topic = redis.getTopic();
        topic.addListenerAsync(String.class, (charSequence, serialized) -> {
            PacketType packetType = PacketType.valueOf(serialized.split(":")[0].toUpperCase());
            String[] packetInfoString = serialized.split(":")[1].split(";");

            switch (packetType) {
                case BLOCK_BREAK:
                    int blockBreakX = Integer.parseInt(packetInfoString[0]);
                    int blockBreakY = Integer.parseInt(packetInfoString[1]);
                    int blockBreakZ = Integer.parseInt(packetInfoString[2]);
                    World blockBreakWorld = Bukkit.getWorld(packetInfoString[3]);

                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        Block blockBreakBlock = blockBreakWorld.getBlockAt(blockBreakX, blockBreakY, blockBreakZ);
                        blockBreakBlock.setType(Material.AIR);
                    });
                    break;
                case BLOCK_PLACE:
                    int blockPlaceX = Integer.parseInt(packetInfoString[0]);
                    int blockPlaceY = Integer.parseInt(packetInfoString[1]);
                    int blockPlaceZ = Integer.parseInt(packetInfoString[2]);
                    World blockPlaceWorld = Bukkit.getWorld(packetInfoString[3]);
                    Material blockPlaceType = Material.valueOf(packetInfoString[4]);

                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        Block blockPlaceBlock = blockPlaceWorld.getBlockAt(blockPlaceX, blockPlaceY, blockPlaceZ);
                        blockPlaceBlock.setType(blockPlaceType);
                    });
                    break;
            }
        });
    }

}
