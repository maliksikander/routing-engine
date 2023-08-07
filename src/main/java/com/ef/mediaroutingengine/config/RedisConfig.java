package com.ef.mediaroutingengine.config;

import com.google.common.collect.Sets;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.util.Pool;

/**
 * This class creates a singleton bean of JedisPool which is used to communicate with the Redis instance.
 */
@Configuration
public class RedisConfig {
    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
    /**
     * Contains all Redis-properties set at the application level.
     */
    private final RedisProperties redisProperties;

    /**
     * Default Constructor. Loads the required beans.
     *
     * @param redisProperties contains the Redis application properties
     */
    @Autowired
    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * Creates and returns a singleton bean of JedisPool. It is used to communicate with the Redis Instance.
     *
     * @return JedisPool bean
     */
    @Bean
    public Pool<Jedis> jedisPool() {

        logger.info("Initializing redis pool ........");
        logger.info("Redis config info {}", redisProperties);
        JedisPoolConfig poolConfig = this.getJedisPoolConfig();
        final Pool<Jedis> jedisPool;
        if (redisProperties.getSentinel().isEnable()) {

            logger.info("Redis Connect with Sentinel");
            Set<String> sentinels = Sets.newHashSet(redisProperties.getSentinel().getNodes());
            JedisSentinelPool jedisSentinelPool = new JedisSentinelPool(redisProperties.getSentinel().getMaster(),
                    sentinels, poolConfig,
                    redisProperties.getTimeout(), redisProperties.getSentinel().getPassword());

            logger.info("Redis sentinel pool initialized on -> {}:{}:{}. Current Master is {}",
                    redisProperties.getSentinel().getNodes(),
                    redisProperties.getSentinel().getMaster(), redisProperties.getSentinel().getPassword(),
                    jedisSentinelPool.getCurrentHostMaster());

            jedisPool = jedisSentinelPool;

        } else {

            jedisPool =
                    new JedisPool(poolConfig, redisProperties.getHost(), redisProperties.getPort(),
                            redisProperties.getTimeout(), redisProperties.getPassword(),
                            redisProperties.isSsl());
            logger.info("Redis pool initialized on ---> {}:{} ........", redisProperties.getHost(),
                    redisProperties.getPort());

        }
        return jedisPool;
    }

    /**
     * Creates and returns an instance of JedisPoolConfig from the application's Redis properties.
     *
     * @return JedisPoolConfig instance.
     */
    private JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisProperties.getMaxActive());
        poolConfig.setMinIdle(redisProperties.getMinIdle());
        poolConfig.setMaxIdle(redisProperties.getMaxIdle());
        poolConfig.setMaxWaitMillis(redisProperties.getMaxWait());
        return poolConfig;
    }
}
