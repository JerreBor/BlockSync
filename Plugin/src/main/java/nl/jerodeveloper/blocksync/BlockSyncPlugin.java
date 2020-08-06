package nl.jerodeveloper.blocksync;

import nl.jerodeveloper.blocksync.listeners.PacketListener;
import nl.jerodeveloper.blocksync.listeners.blocks.*;
import nl.jerodeveloper.blocksync.listeners.players.JoinListener;
import nl.jerodeveloper.blocksync.listeners.players.MoveListener;
import nl.jerodeveloper.blocksync.listeners.players.QuitListener;
import nl.jerodeveloper.blocksync.redis.Redis;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;

public class BlockSyncPlugin extends JavaPlugin {

    private Redis redis;

    @Override
    public void onEnable() {
        ConfigFile configFile = new ConfigFile(this);
        configFile.init();

        for (Config config : EnumSet.allOf(Config.class)) {
            config.setConfigFile(configFile);
        }

        this.redis = new Redis();

        redis.init();

        registerListeners();
    }

    private void registerListeners() {
        final PluginManager pluginManager = getServer().getPluginManager();
        boolean isReceiving = Config.ROLE.getValue().equals("receive");

        System.out.println("Mode is " + (isReceiving ? "receive" : "send"));

        if (isReceiving) {
            new PacketListener(redis, this).listen();
        } else {
            pluginManager.registerEvents(new BlockPlaceListener(redis), this);
            pluginManager.registerEvents(new BlockBreakListener(redis), this);
            pluginManager.registerEvents(new ExplosionListener(redis), this);
            pluginManager.registerEvents(new BurnListener(redis), this);
            pluginManager.registerEvents(new EntityChangeBlockListener(redis), this);
            pluginManager.registerEvents(new MoveListener(redis), this);
            pluginManager.registerEvents(new JoinListener(redis), this);
            pluginManager.registerEvents(new QuitListener(redis), this);
        }

    }

    @Override
    public void onDisable() {
        redis.close();
    }
}
