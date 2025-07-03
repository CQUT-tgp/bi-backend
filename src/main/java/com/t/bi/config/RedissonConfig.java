package com.t.bi.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    private Integer port;
    private String host;
    private String password;
    private Integer database;
    public RedissonClient getRedissonClient() {
        Config config = new Config();
        System.out.println("redis://" + host + ":" + port);
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
//                .setPassword(password)
                .setDatabase(database);
        RedissonClient redissonClient = Redisson.create();
        return redissonClient;
    }
}
