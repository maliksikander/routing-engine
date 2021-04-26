package com.ef.mediaroutingengine.services.redispubsub;

import com.ef.mediaroutingengine.dto.RedisEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class RedisMessageSubscriber implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMessageSubscriber.class);

    /**
     * Receives messages on Redis Pub-Sub channel between routing-engine and Agent-manager.
     *
     * @param message Redis-Message object. Contains message-body and Channel-name
     * @param pattern Pattern object is not being used currently
     */
    public void onMessage(@NotNull Message message, byte[] pattern) {
        LOGGER.info("-------- REDIS SUBSCRIBER CALLED");
        try {
            Object body = this.toObject(message.getBody());
            String channel = this.toString(message.getChannel());

            if (body instanceof RedisEvent) {
                RedisEvent redisEvent = (RedisEvent) body;
                LOGGER.info("Message received: {} on channel: {}", redisEvent, channel);
            } else {
                LOGGER.info("Invalid message body type received. Message is ignored");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object toObject(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        return new ObjectInputStream(in).readObject();
    }

    private String toString(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }
}