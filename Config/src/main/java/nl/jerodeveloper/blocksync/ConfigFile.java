package nl.jerodeveloper.blocksync;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

public class ConfigFile {

    @Getter private File file;
    @Getter private FileConfiguration configuration;
    private final Plugin plugin;

    public ConfigFile(Plugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        this.file = new File(plugin.getDataFolder(), "config.yml");

        if (!this.file.exists()) {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            try {
                this.file.createNewFile();
                this.configuration = YamlConfiguration.loadConfiguration(this.file);
                for (Config config : EnumSet.allOf(Config.class)) {
                    getConfiguration().set(config.getKey(), config.getDefaultValue());
                }

                save();
            } catch (IOException e) {
                System.out.println("Something went wrong while creating config file");
                e.printStackTrace();
            }
        } else {
            this.configuration = YamlConfiguration.loadConfiguration(getFile());
        }
    }

    public void save() {
        try {
            getConfiguration().save(getFile());
        } catch (IOException e) {
            System.out.println("Something went wrong while saving config file");
            e.printStackTrace();
        }
    }

}
