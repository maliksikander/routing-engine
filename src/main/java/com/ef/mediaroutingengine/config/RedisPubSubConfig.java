package com.ef.mediaroutingengine.config;

import com.ef.mediaroutingengine.services.redispubsub.RedisMessageSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisPubSubConfig {
    private final RedisProperties redisProperties;

    @Autowired
    public RedisPubSubConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisProperties.getMaxActive());
        poolConfig.setMinIdle(redisProperties.getMinIdle());
        poolConfig.setMaxIdle(redisProperties.getMaxIdle());
        poolConfig.setMaxWaitMillis(redisProperties.getMaxWait());

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setPoolConfig(poolConfig);
        jedisConnectionFactory.setHostName(redisProperties.getHost());
        jedisConnectionFactory.setPort(redisProperties.getPort());
        jedisConnectionFactory.setTimeout(redisProperties.getTimeout());
        jedisConnectionFactory.setPassword(redisProperties.getPassword());
        jedisConnectionFactory.setUseSsl(redisProperties.isSsl());
        return jedisConnectionFactory;
    }

    /**
     * This bean provides an abstraction over redis commands.
     *
     * @return RedisTemplate singleton bean
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    /**
     * This bean works as a subscriber in the Redis Pub-Sub model.
     *
     * @return message listener adaptor
     */
    @Bean
    MessageListenerAdapter messageListener() {
        return new MessageListenerAdapter(new RedisMessageSubscriber());
    }

    @Bean
    RedisMessageListenerContainer redisContainer() {
        RedisMessageListenerContainer container
                = new RedisMessageListenerContainer();
        container.setConnectionFactory(jedisConnectionFactory());
        container.addMessageListener(messageListener(), topic());
        return container;
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic(redisProperties.getChannelTopic());
    }
}
