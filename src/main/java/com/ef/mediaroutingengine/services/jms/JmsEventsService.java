package com.ef.mediaroutingengine.services.jms;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskStateChangeRequest;
import com.ef.mediaroutingengine.eventlisteners.taskstate.TaskStateListener;
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
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsEventsService.class);

    /**
     * The Object mapper.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * The Property change support.
     */
    private final PropertyChangeSupport propertyChangeSupport;

    /**
     * Constructor. Loads the required beans.
     *
     * @param taskStateListener Task State event listener.
     */
    @Autowired
    public JmsEventsService(TaskStateListener taskStateListener) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.propertyChangeSupport.addPropertyChangeListener(taskStateListener);
    }

    /**
     * Parses the event and message coming on the topic and pass it to the appropriate event handler method.
     *
     * @param event   String object. The name of the incoming event
     * @param message JMS-Message. the actual message object
     * @throws JMSException            If there is an error in getting the actual object from                      inside the JMS-Message object wrapper
     * @throws JsonProcessingException the json processing exception
     */
    public void handleEvent(Enums.JmsEventName event, Message message)
            throws JMSException, JsonProcessingException {
        LOGGER.debug("handleEvent method started");
        validateJmsMessageInstance(message);

        String textMessageString = ((TextMessage) message).getText();

        LOGGER.debug("Message : {}", textMessageString);
        JsonNode textMessageJson = objectMapper.readTree(textMessageString);
        String dataJsonString = textMessageJson.get("data").toString();

        if (event.equals(Enums.JmsEventName.TASK_STATE_CHANGED)) {
            TaskStateChangeRequest req = objectMapper.readValue(dataJsonString, TaskStateChangeRequest.class);
            propertyChangeSupport.firePropertyChange(Enums.EventName.TASK_STATE.toString(), null, req);
        } else {
            LOGGER.info("Event: {} is ignored by JMS Listener", event);
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
