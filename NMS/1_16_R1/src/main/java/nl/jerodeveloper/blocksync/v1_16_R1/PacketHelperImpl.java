package nl.jerodeveloper.blocksync.v1_16_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_16_R1.*;
import nl.jerodeveloper.blocksync.interfaces.PacketHelper;
import nl.jerodeveloper.blocksync.packets.Packet;
import nl.jerodeveloper.blocksync.packets.PacketInfo;
import nl.jerodeveloper.blocksync.packets.PacketType;
import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketHelperImpl implements PacketHelper {

    private final Map<UUID, ImmutablePair<String, String>> profileCache = new HashMap<>();

    @Override
    public void sendMovementPacket(Redis redis, org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        PacketInfo packetInfo = new PacketInfo(
                to.getX() * 32 - from.getX() * 32,
                to.getY() * 32 - from.getY() * 32,
                to.getZ() * 32 - from.getY() * 32,
                to.getYaw(),
                to.getPitch(),
                to.getY() == to.getWorld().getHighestBlockYAt(to.getBlockX(), to.getBlockZ()),
                player.getUniqueId()
        );

        redis.getTopic().publish(new Packet(PacketType.PLAYER_MOVE, packetInfo));
    }

    @Override
    public void sendLoginPacket(Redis redis, Player player, Location location) {
        ImmutablePair<String, String> textureSignaturePair;

        if (!profileCache.containsKey(player.getUniqueId())) {
            EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            Property textures = nmsPlayer.getProfile().getProperties().get("textures").iterator().next();
            textureSignaturePair = new ImmutablePair<>(textures.getValue(), textures.getSignature());
            profileCache.put(player.getUniqueId(), textureSignaturePair);
        } else {
            textureSignaturePair = profileCache.get(player.getUniqueId());
        }

        PacketInfo packetInfo = new PacketInfo(
                location.getX(),
                location.getY(),
                location.getZ(),
                textureSignaturePair.getLeft(),
                textureSignaturePair.getRight(),
                player.getDisplayName(),
                player.getUniqueId().toString(),
                location.getPitch(),
                location.getYaw(),
                location.getWorld().getName()
        );

        redis.getTopic().publish(new Packet(PacketType.PLAYER_SPAWN, packetInfo));
    }

    @Override
    public void sendLogoutPacket(Redis redis, Player player) {
        PacketInfo packetInfo = new PacketInfo(
                player.getEntityId()
        );

        redis.getTopic().publish(new Packet(PacketType.PLAYER_LOGOUT, packetInfo));
    }

    @Override
    public void receiveLoginPacket(String[] packetInfoString) {
        double x = Double.parseDouble(packetInfoString[0]);
        double y = Double.parseDouble(packetInfoString[1]);
        double z = Double.parseDouble(packetInfoString[2]);
        String texture = packetInfoString[3];
        String signature = packetInfoString[4];
        String displayName = packetInfoString[5];
        UUID uniqueId = UUID.fromString(packetInfoString[6]);
        byte pitch = Byte.parseByte(packetInfoString[7]);
        byte yaw = Byte.parseByte(packetInfoString[8]);
        String worldName = packetInfoString[9];

        GameProfile profile = new GameProfile(uniqueId, displayName);
        profile.getProperties().put("textures", new Property("skin", texture, signature));

        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();
        EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer, profile, new PlayerInteractManager(worldServer));

        entityPlayer.setLocation(x, y, z, yaw, pitch);

        PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                entityPlayer
        );

        PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(entityPlayer);

        sendToAll(infoPacket, spawnPacket);

        System.out.println(String.format("Spawned player: %s", entityPlayer.getName()));
    }

    @Override
    public void receiveMovementPacket(String[] packetInfoString) {
        UUID uniqueId = UUID.fromString(packetInfoString[8]);
        Player player = Bukkit.getPlayer(uniqueId);

        if (player == null) return;

        short deltaX = Short.parseShort(packetInfoString[0]);
        short deltaY = Short.parseShort(packetInfoString[1]);
        short deltaZ = Short.parseShort(packetInfoString[2]);
        byte yaw = Byte.parseByte(packetInfoString[3]);
        byte pitch = Byte.parseByte(packetInfoString[4]);
        boolean onGround = Boolean.parseBoolean(packetInfoString[5]);

        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook movePacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                player.getEntityId(),
                deltaX,
                deltaY,
                deltaZ,
                yaw,
                pitch,
                onGround
        );

        sendToAll(movePacket);
    }

    @Override
    public void receiveLogoutPacket(String[] packetInfoString) {
        PacketPlayOutEntityDestroy entityDestroy = new PacketPlayOutEntityDestroy(Integer.parseInt(packetInfoString[0]));
        sendToAll(entityDestroy);
    }

    private void sendToAll(net.minecraft.server.v1_16_R1.Packet<?>... packets) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) onlinePlayer).getHandle().playerConnection;
            for (net.minecraft.server.v1_16_R1.Packet<?> packet : packets) {
                connection.sendPacket(packet);
            }
        }
    }

}
