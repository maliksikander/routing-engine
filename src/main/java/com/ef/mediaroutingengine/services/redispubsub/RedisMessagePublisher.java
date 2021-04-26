package com.ef.mediaroutingengine.services.redispubsub;

import com.ef.mediaroutingengine.dto.RedisEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class RedisMessagePublisher implements MessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;

    @Autowired
    public RedisMessagePublisher(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
    }

    public void publish(RedisEvent message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}