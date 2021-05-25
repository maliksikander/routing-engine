package com.ef.mediaroutingengine.services.redis;

import com.ef.mediaroutingengine.dto.AgentMrdStateChangedRequest;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.eventlisteners.TaskStateEvent;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.PropertyChangeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPubSub;

@Service
public class ChannelSubscribe {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelSubscribe.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final PropertyChangeSupport propertyChangeSupport;
    private final AgentsPool agentsPool;

    /**
     * Constructor.
     *
     * @param taskStateEvent event listener to listen to changes in tasks' state
     * @param agentsPool the pool of all agents.
     */
    public ChannelSubscribe(TaskStateEvent taskStateEvent, AgentsPool agentsPool) {
        this.agentsPool = agentsPool;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.propertyChangeSupport.addPropertyChangeListener(taskStateEvent);
    }

    /**
     * Returns a JedisPubSubInstance which listens on the Redis channel between routing engine and agent-manager.
     *
     * @return An instance of JedisPubSub.
     */
    public JedisPubSub getJedisPubSubInstance() {
        return new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                LOGGER.info("-------- REDIS SUBSCRIBER CALLED");
                try {
                    JsonNode messageJson = objectMapper.readTree(message);
                    String eventName = messageJson.get("name").asText();
                    String eventData = messageJson.get("data").toString();

                    LOGGER.info("Message received: {} on channel: {}", message, channel);
                    switch (Enums.RedisEventName.valueOf(eventName)) {
                        case TASK_STATE_CHANGED:
                            TaskDto taskDto = objectMapper.readValue(eventData, TaskDto.class);
                            propertyChangeSupport.firePropertyChange(Enums.EventName.TASK_STATE.toString(),
                                    null, taskDto);
                            break;
                        case AGENT_MRD_STATE_CHANGED:
                            AgentMrdStateChangedRequest request = objectMapper
                                    .readValue(eventData, AgentMrdStateChangedRequest.class);
                            Agent agent = agentsPool.findById(request.getAgentId());
                            if (agent != null) {
                                agent.changeMrdState(request);
                            }
                            break;
                        default:
                            LOGGER.info("The type of Data is invalid");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                LOGGER.info("Redis-PubSub | Subscribed on channel: {}", channel);
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                LOGGER.info("Redis-PubSub | Unsubscribed from channel: {}", channel);
            }

        };
    }
}