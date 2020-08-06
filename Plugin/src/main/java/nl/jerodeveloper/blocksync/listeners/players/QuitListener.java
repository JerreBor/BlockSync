package nl.jerodeveloper.blocksync.listeners.players;

import nl.jerodeveloper.blocksync.ImplementationPicker;
import nl.jerodeveloper.blocksync.interfaces.PacketHelper;
import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private final PacketHelper packetHelper = new ImplementationPicker().pickImplementation();
    private final Redis redis;

    public QuitListener(Redis redis) {
        this.redis = redis;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        packetHelper.sendLogoutPacket(redis, event.getPlayer());
    }

}
