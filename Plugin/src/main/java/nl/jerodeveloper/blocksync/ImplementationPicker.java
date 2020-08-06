package nl.jerodeveloper.blocksync;

import lombok.Getter;
import nl.jerodeveloper.blocksync.interfaces.PacketHelper;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImplementationPicker {

    @Getter private final String version = extractNMSVersion();
    private PacketHelper implementation;

    // https://github.com/filoghost/HolographicDisplays/blob/master/Utils/src/main/java/com/gmail/filoghost/holographicdisplays/util/VersionUtils.java
    public static String extractNMSVersion() {
        Matcher matcher = Pattern.compile("v\\d+_\\d+_R\\d+").matcher(Bukkit.getServer().getClass().getPackage().getName());
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    public PacketHelper pickImplementation() {
        if (implementation != null) return implementation;

        try {
            String className = String.format("nl.jerodeveloper.blocksync.%s.PacketHelperImpl", getVersion());
            return this.implementation = (PacketHelper) Class.forName(className).newInstance();
        } catch (Exception e) {
            System.out.println("Could not find valid implementation for version " + version);
            e.printStackTrace();
            return null;
        }
    }

}
