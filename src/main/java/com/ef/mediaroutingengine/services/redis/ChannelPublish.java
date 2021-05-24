package com.ef.mediaroutingengine.services.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

@Service
public class ChannelPublish {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ChannelPublish(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * Publishes a jsonString on the Redis channel between routing engine and agent-manager.
     *
     * @param message The message to publish.
     */
    public void publish(Object message) {
        try {
            String jsonString = objectMapper.writeValueAsString(message);
            this.jedisPool.getResource().publish("REDIS_MESSAGE_CHANNEL", jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}