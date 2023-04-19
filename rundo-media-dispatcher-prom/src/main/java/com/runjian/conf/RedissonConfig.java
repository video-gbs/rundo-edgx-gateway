package com.runjian.conf;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * @author chenjialing
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private String redisPort;

    @Value("${spring.redis.database}")
    private Integer redisDatabase;

    @Value("${spring.redis.password}")
    private String password;

    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        Config config = Config.fromYAML(new ClassPathResource("redisson.yml").getInputStream());
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setDatabase(redisDatabase)
                .setPassword(password);
        return Redisson.create(config);
    }
}
