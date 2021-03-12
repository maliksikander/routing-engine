package com.ef.mediaroutingengine.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Autowired
    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    public JedisPool jedisPool() {
        logger.info("Initializing redis pool ........");

        try {
            logger.info("Redis config info  " + redisProperties.toString());
            if (this.redisProperties.getConnectAtStartup()) {
                return this.createJedisPool();
            }
            return new JedisPool();
        } catch (Exception ex) {
            logger.error("Error in redis pool initialization ", ex);
            throw ex;
        }

    }

    private JedisPool createJedisPool() {
        // Pool Settings
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisProperties.getMaxActive());
        poolConfig.setMinIdle(redisProperties.getMinIdle());
        poolConfig.setMaxIdle(redisProperties.getMaxIdle());
        poolConfig.setMaxWaitMillis(redisProperties.getMaxWait());

        // final JedisPool jedisPool = new JedisPool(poolConfig,redisProperties.getHost(),redisProperties.getPort(),false);
        final JedisPool jedisPool = new JedisPool(poolConfig, redisProperties.getHost(),
                redisProperties.getPort(),
                redisProperties.getTimeout(), redisProperties.getPassword(),
                redisProperties.isSsl());
        // Get Connection from pool to verify if connection is established with redis server
        //jedisPool.getResource();
        //jedisPool.getResource().flushDB();
        //jedisPool.getResource().flushAll();
        logger.info("Redis pool initialized on ---> {}:{} ........", redisProperties.getHost(),
                redisProperties.getPort());
        return jedisPool;
    }

}
