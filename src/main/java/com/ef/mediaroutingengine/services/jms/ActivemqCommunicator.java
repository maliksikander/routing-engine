package com.ef.mediaroutingengine.services.jms;

import com.ef.cim.objectmodel.CimEvent;
import com.ef.cim.objectmodel.CimEventName;
import com.ef.cim.objectmodel.CimEventType;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.StateChangeEvent;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Task;
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
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(ActivemqCommunicator.class);
    /**
     * The constant SUBSCRIBER_NAME.
     */
    private static final String SUBSCRIBER_NAME = "ROUTING-ENGINE-SUBSCRIBER";
    /**
     * The Jms events service.
     */
    private final JmsEventsService jmsEventsService;
    /**
     * The Connection.
     */
    Connection connection;
    /**
     * The Publisher.
     */
    MessageProducer publisher;
    /**
     * The Subscriber session.
     */
    private Session subscriberSession;
    /**
     * The Publisher session.
     */
    private Session publisherSession;
    /**
     * The Subscriber.
     */
    private MessageConsumer subscriber;
    /**
     * The Topic name.
     */
    private String topicName;

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
     * @param connection       the connection
     * @param jmsEventsService the jms events service
     * @throws JMSException the jms exception
     */
    @Autowired
    public ActivemqCommunicator(Connection connection, JmsEventsService jmsEventsService) throws JMSException {
        logger.debug("ActivemqServiceImpl.Constructor started");
        this.connection = connection;
        this.connection.setExceptionListener(this);
        this.jmsEventsService = jmsEventsService;
        this.objectMapper.findAndRegisterModules();
        logger.debug("ActivemqServiceImpl.Constructor ended");
    }

    @Override
    public void init(String topic) throws JMSException {
        logger.debug("ActivemqServiceImpl.init method started");
        if (topic == null) {
            throw new IllegalArgumentException("Topic is null");
        }
        this.topicName = topic;

        try {
            this.setSubscriber();
            this.setPublisher();
            logger.info("Connection, subscriber, publisher successfully "
                    + "initialized for topic '{}'", this.topicName);
        } catch (JMSException jmsException) {
            logger.error(
                    "ActivemqServiceImpl.init | JMSException, trying to close connection and sessions...");
            this.stop();
            throw jmsException;
        }

        logger.debug("ActivemqServiceImpl.init method ended");
    }

    @Override
    public void publish(Serializable message, Enums.JmsEventName eventName)
            throws JMSException, JsonProcessingException {
        logger.debug("method started");

        StateChangeEvent stateChangeEvent = new StateChangeEvent(eventName, message, this.topicName);

        String messageStr = this.objectMapper.writeValueAsString(stateChangeEvent);

        TextMessage messageToSend = this.publisherSession.createTextMessage();
        messageToSend.setText(messageStr);
        messageToSend.setJMSType(eventName.toString());

        this.publisher.send(messageToSend);

        logger.info("Text Message: '{}' published on topic: '{}'", messageStr, this.topicName);
        logger.debug("method ended");
    }

    @Override
    public void publishTaskStateChangeForReporting(Task task) {
        String topic = task.getTopicId().toString();
        try (
                Session session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(session.createTopic(topic))
        ) {
            String messageStr = this.getSerializedCimEvent(new TaskDto(task));
            TextMessage messageToSend = session.createTextMessage();
            messageToSend.setText(messageStr);
            messageToSend.setJMSType(CimEventName.TASK_STATE_CHANGED.name());

            producer.send(messageToSend);
        } catch (Exception e) {
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
    private String getSerializedCimEvent(Serializable message) throws JsonProcessingException {
        CimEvent cimEvent = new CimEvent(message, CimEventName.TASK_STATE_CHANGED, CimEventType.NOTIFICATION);
        return this.objectMapper.writeValueAsString(cimEvent);
    }

    @Override
    public void stop() throws JMSException {
        logger.debug("ActivemqServiceImpl.stop method started");
        if (this.publisher != null) {
            this.publisher.close();
            this.publisher = null;
            logger.debug("ActivemqServiceImpl.stop | publisher closed");
        }
        if (this.publisherSession != null) {
            this.publisherSession.close();
            this.publisherSession = null;
            logger.debug("ActivemqServiceImpl.stop | publisherSession closed");
        }
        if (subscriber != null) {
            subscriber.close();
            this.subscriber = null;
            logger.debug("ActivemqServiceImpl.stop | subscriber closed");
        }
        if (subscriberSession != null) {
            this.subscriberSession.unsubscribe(SUBSCRIBER_NAME);
            subscriberSession.close();
            this.subscriberSession = null;
            logger.debug("ActivemqServiceImpl.stop | subscriberSession closed");
        }

        logger.info("Communication stopped successfully on Topic: '{}'", this.topicName);
        logger.debug("ActivemqServiceImpl.stop method ended");
    }

    @Override
    public void onMessage(Message message) {
        logger.debug("ActivemqServiceImpl.onMessage method started");
        try {
            Enums.JmsEventName event = Enums.JmsEventName.valueOf(message.getJMSType());
            System.out.println("*******************************");
            System.out.println("JMS EVENT: " + event + " received");
            System.out.println("*******************************");
            this.jmsEventsService.handleEvent(event, message);
            message.acknowledge();

            logger.info("ActivemqServiceImpl.onMessage |  Event: '{}' handled gracefully "
                    + "on Topic: '{}'", event, this.topicName);
        } catch (JMSException | JsonProcessingException e) {
            logger.error("Exception while handling JMS Message: ", e);
        }

        logger.debug("ActivemqServiceImpl.onMessage method ended");
    }

    @Override
    public synchronized void onException(JMSException ex) {
        logger.debug("ActivemqServiceImpl.onException method started");
        logger.error("JMSException: ", ex);
        logger.debug("ActivemqServiceImpl.onException method ended");
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
        logger.debug("Method started for Topic: '{}'", this.topicName);

        this.subscriberSession = this.connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        logger.debug("ActivemqServiceImpl.setSubscriber | Session created successfully");
        Topic destination = this.subscriberSession.createTopic(this.topicName);
        logger.debug("ActivemqServiceImpl.setSubscriber | Topic Destination created successfully");
        this.subscriber = subscriberSession.createDurableSubscriber(destination, SUBSCRIBER_NAME);
        logger.debug("ActivemqServiceImpl.setSubscriber | Durable subscriber created successfully");
        this.subscriber.setMessageListener(this);
        logger.debug("Method ended for Topic: '{}'", this.topicName);
    }

    /**
     * Used by the init() method to create a new topic publisher on the connection.
     *
     * @throws JMSException exception
     */
    private void setPublisher() throws JMSException {
        logger.debug("Method started for Topic: '{}'", this.topicName);

        this.publisherSession = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        logger.debug("ActivemqServiceImpl.setPublisher | Session created successfully");
        Topic destination = this.publisherSession.createTopic(this.topicName);
        this.publisher = this.publisherSession.createProducer(destination);
        logger.debug("ActivemqServiceImpl.setPublisher | Publisher created successfully");
        logger.debug("Method ended for Topic: '{}'", this.topicName);
    }
}