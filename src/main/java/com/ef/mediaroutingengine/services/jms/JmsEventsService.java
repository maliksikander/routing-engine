package com.ef.mediaroutingengine.services.jms;

import com.ef.mediaroutingengine.dto.AgentMrdStateChangedRequest;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.eventlisteners.TaskStateEvent;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.PropertyChangeSupport;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles events coming on the JMS broker.
 * Uses the EventHandler class to handle the events.
 */
@Service
public class JmsEventsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsEventsService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PropertyChangeSupport propertyChangeSupport;
    private final AgentsPool agentsPool;

    /**
     * Constructor. Loads the required beans.
     *
     * @param agentsPool Agents pool bean.
     * @param taskStateEvent Task State event listener.
     */
    @Autowired
    public JmsEventsService(AgentsPool agentsPool, TaskStateEvent taskStateEvent) {
        this.agentsPool = agentsPool;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.propertyChangeSupport.addPropertyChangeListener(taskStateEvent);
    }

    /**
     * Parses the event and message coming on the topic and pass it to the appropriate event handler method.
     *
     * @param event   String object. The name of the incoming event
     * @param message JMS-Message. the actual message object
     * @throws JMSException If there is an error in getting the actual object from
     *                      inside the JMS-Message object wrapper
     */
    public void handleEvent(String event, Message message) throws JMSException, JsonProcessingException {
        LOGGER.debug("handleEvent method started");
        validateJmsMessageInstance(message);

        String textMessageString = ((TextMessage) message).getText();

        LOGGER.debug("Message : {}", textMessageString);
        JsonNode textMessageJson = objectMapper.readTree(textMessageString);
        String dataJsonString = textMessageJson.get("data").toString();

        switch (Enums.RedisEventName.valueOf(event)) {
            case TASK_STATE_CHANGED:
                TaskDto taskDto = objectMapper.readValue(dataJsonString, TaskDto.class);
                propertyChangeSupport.firePropertyChange(Enums.EventName.TASK_STATE.toString(),
                        null, taskDto);
                break;
            case AGENT_STATE_CHANGED:
                LOGGER.info("Agent state event");
                break;
            case AGENT_MRD_STATE_CHANGED:
                AgentMrdStateChangedRequest request = objectMapper
                        .readValue(dataJsonString, AgentMrdStateChangedRequest.class);
                Agent agent = agentsPool.findById(request.getAgentId());
                if (agent != null) {
                    agent.changeMrdState(request);
                }
                break;
            default:
                LOGGER.info("The type of Data is invalid");
        }

        LOGGER.debug("handleEvent method ended");
    }

    /**
     * Checks is the JMS Message is an instance of JMS ObjectMessage <br>
     * Throws IllegalArgumentException if it is not.
     *
     * @param message JMS Message
     */
    private void validateJmsMessageInstance(Message message) {
        LOGGER.debug("validateJmsMessageInstance method started");

        if (!(message instanceof TextMessage)) {
            throw new IllegalArgumentException(
                    "The JMS-Message object should be instance of JMS-Text-Message");
        }

        LOGGER.debug("validateJmsMessageInstance method ended");
    }

}
