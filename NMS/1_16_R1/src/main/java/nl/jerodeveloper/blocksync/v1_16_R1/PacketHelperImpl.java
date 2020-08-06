package nl.jerodeveloper.blocksync.v1_16_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_16_R1.*;
import nl.jerodeveloper.blocksync.Pair;
import nl.jerodeveloper.blocksync.interfaces.PacketHelper;
import nl.jerodeveloper.blocksync.packets.Packet;
import nl.jerodeveloper.blocksync.packets.PacketInfo;
import nl.jerodeveloper.blocksync.packets.PacketType;
import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketHelperImpl implements PacketHelper {

    private final Map<UUID, Pair<String, String>> profileCache = new HashMap<>(); // send
    private final Map<UUID, Integer> entityIdCache = new HashMap<>(); // receive

    @Override
    public void sendMovementPacket(Redis redis, org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Location from = event.getFrom();
        Location to = event.getTo();

        PacketInfo packetInfo;
        PacketType packetType;

        if (to == null) return;

        if ((to.getYaw() != from.getYaw() || from.getYaw() != from.getYaw()) && (from.getX() != to.getX() || from.getY() != to.getY() || from.getX() != to.getX())) { // move look
            packetInfo = new PacketInfo(
                    ((short) (to.getX() * 32 - from.getX() * 32) * 128),
                    ((short) (to.getY() * 32 - from.getY() * 32) * 128),
                    ((short) (to.getZ() * 32 - from.getZ() * 32) * 128),
                    to.getYaw(),
                    to.getPitch(),
                    true,
                    player.getUniqueId()
            );
            packetType = PacketType.PLAYER_MOVE_LOOK;
        } else if ((to.getYaw() != from.getYaw() || from.getYaw() != from.getYaw())) { // look
            packetInfo = new PacketInfo(
                    to.getYaw(),
                    to.getPitch(),
                    player.getUniqueId(),
                    true
            );

            packetType = PacketType.PLAYER_LOOK;
        } else { // move
            packetInfo = new PacketInfo(
                    ((short) (to.getX() * 32 - from.getX() * 32) * 128),
                    ((short) (to.getY() * 32 - from.getY() * 32) * 128),
                    ((short) (to.getZ() * 32 - from.getZ() * 32) * 128),
                    player.getUniqueId(),
                    true
            );
            packetType = PacketType.PLAYER_MOVE;
        }

        Packet packet = new Packet(packetType, packetInfo);
        redis.getTopic().publish(packet.serialize());
    }

    @Override
    public void sendLoginPacket(Redis redis, Player player, Location location) {
        Pair<String, String> textureSignaturePair;

        if (!profileCache.containsKey(player.getUniqueId())) {
            EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            System.out.println(nmsPlayer.getProfile().getProperties().get("textures"));
            Property textures = nmsPlayer.getProfile().getProperties().get("textures").iterator().next();
            textureSignaturePair = Pair.of(textures.getValue(), textures.getSignature());
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

        Packet packet = new Packet(PacketType.PLAYER_SPAWN, packetInfo);
        redis.getTopic().publish(packet.serialize());
    }

    @Override
    public void sendLogoutPacket(Redis redis, Player player) {
        PacketInfo packetInfo = new PacketInfo(
                player.getUniqueId()
        );

        Packet packet = new Packet(PacketType.PLAYER_LOGOUT, packetInfo);
        redis.getTopic().publish(packet.serialize());
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
        float pitch = Float.parseFloat(packetInfoString[7]);
        float yaw = Float.parseFloat(packetInfoString[8]);
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

        entityIdCache.put(uniqueId, entityPlayer.getId());

        System.out.println(String.format("Spawned player: %s", entityPlayer.getName()));
    }

    @Override
    public void receiveMoveLookPacket(String[] packetInfoString) {
        short deltaX = Short.parseShort(packetInfoString[0]);
        short deltaY = Short.parseShort(packetInfoString[1]);
        short deltaZ = Short.parseShort(packetInfoString[2]);
        float yaw = Float.parseFloat(packetInfoString[3]);
        float pitch = Float.parseFloat(packetInfoString[4]);
        boolean onGround = Boolean.parseBoolean(packetInfoString[5]);
        int entityId = entityIdCache.get(UUID.fromString(packetInfoString[6]));

        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook movePacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                entityId,
                deltaX,
                deltaY,
                deltaZ,
                (byte) yaw,
                (byte) pitch,
                onGround
        );

        sendToAll(movePacket);
    }

    @Override
    public void receiveLookPacket(String[] packetInfoString) {
        float yaw = Float.parseFloat(packetInfoString[0]);
        float pitch = Float.parseFloat(packetInfoString[1]);
        int entityId = entityIdCache.get(UUID.fromString(packetInfoString[2]));
        boolean onGround = Boolean.parseBoolean(packetInfoString[3]);

        PacketPlayOutEntity.PacketPlayOutEntityLook look = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                entityId,
                (byte) yaw,
                (byte) pitch,
                onGround
        );

        sendToAll(look);
    }

    @Override
    public void receiveMovePacket(String[] packetInfoString) {
        short deltaX = Short.parseShort(packetInfoString[0]);
        short deltaY = Short.parseShort(packetInfoString[1]);
        short deltaZ = Short.parseShort(packetInfoString[2]);
        int entityId = entityIdCache.get(UUID.fromString(packetInfoString[3]));
        boolean onGround = Boolean.parseBoolean(packetInfoString[4]);

        PacketPlayOutEntity.PacketPlayOutRelEntityMove move = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                entityId,
                deltaX,
                deltaY,
                deltaZ,
                onGround
        );

        sendToAll(move);
    }

    @Override
    public void receiveLogoutPacket(String[] packetInfoString) {
        UUID uniqueId = UUID.fromString(packetInfoString[0]);
        PacketPlayOutEntityDestroy entityDestroy = new PacketPlayOutEntityDestroy(entityIdCache.get(uniqueId));
        sendToAll(entityDestroy);
        entityIdCache.remove(uniqueId);
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
