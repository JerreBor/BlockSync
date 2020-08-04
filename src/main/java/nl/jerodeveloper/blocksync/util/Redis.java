package nl.jerodeveloper.blocksync.util;

import lombok.Getter;
import nl.jerodeveloper.blocksync.packets.Packet;
import org.redisson.Redisson;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

public class Redis {

    @Getter private RQueue<String> packetQueue;
    @Getter RedissonClient redissonClient;

    public void init() {
        System.out.println("Connecting to redis...");
        Config config = new Config();

        config.useSingleServer().setAddress((String) nl.jerodeveloper.blocksync.util.Config.REDIS_URL.getValue());
        this.redissonClient = Redisson.create(config);

        if (redissonClient.isShutdown() || redissonClient.isShuttingDown()) {
            System.out.println("Could not connect to redis. Is it on?");
            return;
        }

        this.packetQueue = redissonClient.getQueue("packets");
        packetQueue.clear();

        System.out.println("Connected to redis");
    }

    public void close() {
        if (!redissonClient.isShutdown() && !redissonClient.isShuttingDown()) {
            redissonClient.shutdown();
            System.out.println("Closed redis connection");
        }
    }

}
