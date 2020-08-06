package nl.jerodeveloper.blocksync.listeners.players;

import nl.jerodeveloper.blocksync.ImplementationPicker;
import nl.jerodeveloper.blocksync.interfaces.PacketHelper;
import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final PacketHelper packetHelper = new ImplementationPicker().pickImplementation();
    private final Redis redis;

    public JoinListener(Redis redis) {
        this.redis = redis;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        packetHelper.sendLoginPacket(redis, event.getPlayer(), event.getPlayer().getLocation());
    }

}
