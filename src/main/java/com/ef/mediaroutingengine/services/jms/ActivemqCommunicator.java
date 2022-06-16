package com.ef.mediaroutingengine.services.jms;

import com.ef.cim.objectmodel.CimEvent;
import com.ef.cim.objectmodel.CimEventName;
import com.ef.cim.objectmodel.CimEventType;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.commons.Constants;
import com.ef.mediaroutingengine.dto.StateChangeEvent;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.utilities.AdapterUtility;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * An implementation of the JmsCommunicator interface. Handles message communication
 * for a particular customer between other CIM microservices and bot framework over
 * an activemq broker.
 */
@Service
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ActivemqCommunicator implements JmsCommunicator {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(ActivemqCommunicator.class);
    /**
     * The Connection.
     */
    Connection connection;
    /**
     * The State Change Event Publisher Session.
     */
    private Session stateChangeEventPublisherSession;
    /**
     * The State Change Event Publisher.
     */
    MessageProducer stateChangeEventPublisher;
    /**
     * The Conversation Event Publisher Session.
     */
    private Session conversationEventPublisherSession;
    /**
     * The Conversation event publisher.
     */
    MessageProducer conversationEventPublisher;
    /**
     * The Topics.
     */
    private final List<String> topics = new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor
     *
     * <p>sets the activemq properties taken from the application-prod.properties
     *
     * <p>Sets the broker_url for the amq connection using the activemq's
     * 'failover' transport
     * //* @param amqProperties carries all the system defined activemq properties.
     *
     * @param connection the connection
     * @throws JMSException the jms exception
     */
    @Autowired
    public ActivemqCommunicator(Connection connection) throws JMSException {
        this.connection = connection;
        this.connection.setExceptionListener(this);
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void init(String stateChangeTopic, String conversationTopic) throws JMSException {
        logger.debug(Constants.METHOD_STARTED);

        this.topics.add(stateChangeTopic);
        this.topics.add(conversationTopic);

        try {
            this.stateChangeEventPublisherSession = createPublisherSession();
            this.stateChangeEventPublisher = createPublisher(stateChangeEventPublisherSession, stateChangeTopic);

            this.conversationEventPublisherSession = createPublisherSession();
            this.conversationEventPublisher = createPublisher(conversationEventPublisherSession, conversationTopic);

            logger.info("Publisher successfully initialized for topics: '{}' and '{}'",
                    stateChangeTopic, conversationTopic);
        } catch (JMSException jmsException) {
            logger.error(ExceptionUtils.getMessage(jmsException));
            logger.error(ExceptionUtils.getStackTrace(jmsException));
            this.stop();
            throw jmsException;
        }

        logger.debug(Constants.METHOD_ENDED);
    }

    @Override
    public void publish(Serializable message, Enums.JmsEventName eventName)
            throws JMSException, JsonProcessingException {
        logger.debug(Constants.METHOD_STARTED);

        StateChangeEvent stateChangeEvent = new StateChangeEvent(eventName, message, this.topics.get(0));

        String messageStr = this.objectMapper.writeValueAsString(stateChangeEvent);

        TextMessage messageToSend = this.stateChangeEventPublisherSession.createTextMessage();
        messageToSend.setText(messageStr);
        messageToSend.setJMSType(eventName.name());
        messageToSend.setJMSCorrelationID(MDC.get(Constants.MDC_CORRELATION_ID));

        this.stateChangeEventPublisher.send(messageToSend);

        logger.info("Jms event: '{}' with payload: '{}' published on topic: '{}'",
                eventName, messageStr, topics.get(0));
        logger.debug(Constants.METHOD_ENDED);
    }

    @Override
    public void publishTaskStateChangeForReporting(Task task) {
        try {
            if (task.isMarkedForDeletion()) {
                task.setTaskStateFromMarkedForDeletion();
            }

            String messageStr = this.getSerializedCimEvent(AdapterUtility.createTaskDtoFrom(task), task.getTopicId());
            TextMessage messageToSend = this.conversationEventPublisherSession.createTextMessage();
            messageToSend.setText(messageStr);

            messageToSend.setJMSType(CimEventName.TASK_STATE_CHANGED.name());
            messageToSend.setJMSCorrelationID(MDC.get(Constants.MDC_CORRELATION_ID));

            conversationEventPublisher.send(messageToSend);

            logger.info("Jms event: '{}' with payload: '{}' published on topic: '{}'",
                    CimEventName.TASK_STATE_CHANGED, messageStr, topics.get(1));
        } catch (JMSException | JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets serialized cim event.
     *
     * @param message the message
     * @return the serialized cim event
     * @throws JsonProcessingException the json processing exception
     */
    private String getSerializedCimEvent(Serializable message, UUID conversationId) throws JsonProcessingException {
        CimEvent cimEvent = new CimEvent(message, CimEventName.TASK_STATE_CHANGED, CimEventType.NOTIFICATION,
                conversationId);
        return this.objectMapper.writeValueAsString(cimEvent);
    }

    @Override
    public void stop() throws JMSException {
        logger.debug(Constants.METHOD_STARTED);

        this.stopPublisher(stateChangeEventPublisher);
        this.stopPublisherSession(stateChangeEventPublisherSession);

        this.stopPublisher(conversationEventPublisher);
        this.stopPublisherSession(conversationEventPublisherSession);

        logger.info("Communication stopped successfully on all topics");

        logger.debug(Constants.METHOD_ENDED);
    }

    private void stopPublisher(MessageProducer publisher) throws JMSException {
        if (publisher != null) {
            publisher.close();
        }
    }

    private void stopPublisherSession(Session publisherSession) throws JMSException {
        if (publisherSession != null) {
            publisherSession.close();
        }
    }

    @Override
    public synchronized void onException(JMSException ex) {
        logger.error(ExceptionUtils.getMessage(ex));
        logger.error(ExceptionUtils.getStackTrace(ex));
    }

    private Session createPublisherSession() throws JMSException {
        return this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Used by the init() method to create a new topic publisher on the connection.
     *
     * @throws JMSException exception
     */
    private MessageProducer createPublisher(Session publisherSession, String topicName) throws JMSException {
        logger.debug("Method started for Topic: '{}'", topicName);

        Topic destination = publisherSession.createTopic(topicName);
        MessageProducer publisher = this.stateChangeEventPublisherSession.createProducer(destination);
        logger.debug("Publisher created successfully");

        logger.debug("Method ended for Topic: '{}'", topicName);
        return publisher;
    }
}