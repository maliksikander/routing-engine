package com.ef.mediaroutingengine.services.jms;

import com.ef.mediaroutingengine.dto.StateChangeEvent;
import com.ef.mediaroutingengine.model.Enums;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivemqCommunicator.class);
    Connection connection;
    MessageProducer publisher;
    private Session subscriberSession;
    private Session publisherSession;
    private MessageConsumer subscriber;
    private String topicName;
    private static final String SUBSCRIBER_NAME = "ROUTING-ENGINE-SUBSCRIBER";
    private final JmsEventsService jmsEventsService;

    /**
     * Constructor
     *
     * <p>sets the activemq properties taken from the application-prod.properties
     *
     * <p>Sets the broker_url for the amq connection using the activemq's
     * 'failover' transport
     * //* @param amqProperties carries all the system defined activemq properties.
     */
    @Autowired
    public ActivemqCommunicator(Connection connection, JmsEventsService jmsEventsService) throws JMSException {
        LOGGER.debug("ActivemqServiceImpl.Constructor started");
        this.connection = connection;
        this.connection.setExceptionListener(this);
        this.jmsEventsService = jmsEventsService;
        LOGGER.debug("ActivemqServiceImpl.Constructor ended");
    }

    @Override
    public void init(String topic) throws JMSException {
        LOGGER.debug("ActivemqServiceImpl.init method started");
        if (topic == null) {
            throw new IllegalArgumentException("Topic is null");
        }
        this.topicName = topic;

        try {
            this.setSubscriber();
            this.setPublisher();
            LOGGER.info("Connection, subscriber, publisher successfully "
                    + "initialized for topic '{}'", this.topicName);
        } catch (JMSException jmsException) {
            LOGGER.error(
                    "ActivemqServiceImpl.init | JMSException, trying to close connection and sessions...");
            this.stop();
            throw jmsException;
        }

        LOGGER.debug("ActivemqServiceImpl.init method ended");
    }

    @Override
    public void publish(Serializable message, Enums.RedisEventName eventName)
            throws JMSException, JsonProcessingException {
        LOGGER.debug("method started");

        StateChangeEvent stateChangeEvent = new StateChangeEvent(eventName, message);
        ObjectMapper objectMapper = new ObjectMapper();
        String messageStr = objectMapper.writeValueAsString(stateChangeEvent);

        TextMessage messageToSend = this.publisherSession.createTextMessage();
        messageToSend.setText(messageStr);
        messageToSend.setJMSType(eventName.toString());

        this.publisher.send(messageToSend);

        LOGGER.info("Text Message: '{}' published on topic: '{}'", messageStr, this.topicName);
        LOGGER.debug("method ended");
    }

    @Override
    public void stop() throws JMSException {
        LOGGER.debug("ActivemqServiceImpl.stop method started");
        if (this.publisher != null) {
            this.publisher.close();
            this.publisher = null;
            LOGGER.debug("ActivemqServiceImpl.stop | publisher closed");
        }
        if (this.publisherSession != null) {
            this.publisherSession.close();
            this.publisherSession = null;
            LOGGER.debug("ActivemqServiceImpl.stop | publisherSession closed");
        }
        if (subscriber != null) {
            subscriber.close();
            this.subscriber = null;
            LOGGER.debug("ActivemqServiceImpl.stop | subscriber closed");
        }
        if (subscriberSession != null) {
            this.subscriberSession.unsubscribe(SUBSCRIBER_NAME);
            subscriberSession.close();
            this.subscriberSession = null;
            LOGGER.debug("ActivemqServiceImpl.stop | subscriberSession closed");
        }

        LOGGER.info("Communication stopped successfully on Topic: '{}'", this.topicName);
        LOGGER.debug("ActivemqServiceImpl.stop method ended");
    }

    @Override
    public void onMessage(Message message) {
        LOGGER.debug("ActivemqServiceImpl.onMessage method started");

        try {
            String event = message.getJMSType();
            this.jmsEventsService.handleEvent(event, message);
            message.acknowledge();

            LOGGER.info("ActivemqServiceImpl.onMessage |  Event: '{}' handled gracefully "
                    + "on Topic: '{}'", event, this.topicName);
        } catch (JMSException | JsonProcessingException e) {
            LOGGER.error("Exception while handling JMS Message: ", e);
        }

        LOGGER.debug("ActivemqServiceImpl.onMessage method ended");
    }

    @Override
    public synchronized void onException(JMSException ex) {
        LOGGER.debug("ActivemqServiceImpl.onException method started");
        LOGGER.error("JMSException: ", ex);
        LOGGER.debug("ActivemqServiceImpl.onException method ended");
    }

    public String getTopic() {
        return this.topicName;
    }

    /**
     * Used by the init() method to create a new topic subscriber on the connection.
     * Creates a 'durable subscriber'. Uses MessageListener's onMessage method to receive messages.
     *
     * @throws JMSException exception
     */
    private void setSubscriber() throws JMSException {
        LOGGER.debug("Method started for Topic: '{}'", this.topicName);

        this.subscriberSession = this.connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        LOGGER.debug("ActivemqServiceImpl.setSubscriber | Session created successfully");
        Topic destination = this.subscriberSession.createTopic(this.topicName);
        LOGGER.debug("ActivemqServiceImpl.setSubscriber | Topic Destination created successfully");
        this.subscriber = subscriberSession.createDurableSubscriber(destination, SUBSCRIBER_NAME);
        LOGGER.debug("ActivemqServiceImpl.setSubscriber | Durable subscriber created successfully");
        this.subscriber.setMessageListener(this);
        LOGGER.debug("Method ended for Topic: '{}'", this.topicName);
    }

    /**
     * Used by the init() method to create a new topic publisher on the connection.
     *
     * @throws JMSException exception
     */
    private void setPublisher() throws JMSException {
        LOGGER.debug("Method started for Topic: '{}'", this.topicName);

        this.publisherSession = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        LOGGER.debug("ActivemqServiceImpl.setPublisher | Session created successfully");
        Topic destination = this.publisherSession.createTopic(this.topicName);
        this.publisher = this.publisherSession.createProducer(destination);
        LOGGER.debug("ActivemqServiceImpl.setPublisher | Publisher created successfully");
        LOGGER.debug("Method ended for Topic: '{}'", this.topicName);
    }
}