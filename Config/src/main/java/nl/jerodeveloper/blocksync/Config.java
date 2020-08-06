package nl.jerodeveloper.blocksync;

import lombok.Getter;
import lombok.Setter;

public enum Config {

    REDIS_URL("redis.url", "redis://127.0.0.1:6379"),
    ROLE("role", "receive")
    ;

    @Getter private final String key;
    @Getter private final Object defaultValue;
    private Object value = null;
    @Setter private ConfigFile configFile;

    Config(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public Object getValue() {
        if (value == null) {
            value = configFile.getConfiguration().get(getKey());

            if (value == null) {
                value = defaultValue;
                configFile.getConfiguration().set(getKey(), getDefaultValue());
                configFile.save();
            }
        }
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        configFile.save();
    }

}
