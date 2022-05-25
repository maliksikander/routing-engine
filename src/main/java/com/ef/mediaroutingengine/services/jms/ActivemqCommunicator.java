package com.ef.mediaroutingengine.services.jms;

import com.ef.cim.objectmodel.CimEvent;
import com.ef.cim.objectmodel.CimEventName;
import com.ef.cim.objectmodel.CimEventType;
import com.ef.mediaroutingengine.commons.Constants;
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
     * The Publisher.
     */
    MessageProducer publisher;
    /**
     * The Publisher session.
     */
    private Session publisherSession;
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
    public void init(String topic) throws JMSException {
        logger.debug(Constants.METHOD_STARTED);
        if (topic == null) {
            String errorMessage = "Topic is null";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        this.topicName = topic;

        try {
            this.setPublisher();
            logger.info("Publisher successfully initialized for topic '{}'", this.topicName);
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

        StateChangeEvent stateChangeEvent = new StateChangeEvent(eventName, message, this.topicName);

        String messageStr = this.objectMapper.writeValueAsString(stateChangeEvent);

        TextMessage messageToSend = this.publisherSession.createTextMessage();
        messageToSend.setText(messageStr);
        messageToSend.setJMSType(eventName.name());
        messageToSend.setJMSCorrelationID(MDC.get(Constants.MDC_CORRELATION_ID));

        this.publisher.send(messageToSend);

        logger.info("Jms event: '{}' with payload: '{}' published on topic: '{}'", eventName, messageStr, topicName);
        logger.debug(Constants.METHOD_ENDED);
    }

    @Override
    public void publishTaskStateChangeForReporting(Task task) {
        String topic = task.getTopicId().toString();
        try (
                Session session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(session.createTopic(topic))
        ) {

            if (task.isMarkedForDeletion()) {
                task.setTaskStateFromMarkedForDeletion();
            }

            String messageStr = this.getSerializedCimEvent(new TaskDto(task));
            TextMessage messageToSend = session.createTextMessage();
            messageToSend.setText(messageStr);
            messageToSend.setJMSType(CimEventName.TASK_STATE_CHANGED.name());
            messageToSend.setJMSCorrelationID(MDC.get(Constants.MDC_CORRELATION_ID));

            producer.send(messageToSend);
            logger.info("Jms event: '{}' with payload: '{}' published on topic: '{}'",
                    CimEventName.TASK_STATE_CHANGED, messageStr, topicName);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
            logger.error(ExceptionUtils.getStackTrace(e));
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
        logger.debug(Constants.METHOD_STARTED);

        if (this.publisher != null) {
            this.publisher.close();
            this.publisher = null;
            logger.debug("Publisher closed on topic: {}", this.topicName);
        }
        if (this.publisherSession != null) {
            this.publisherSession.close();
            this.publisherSession = null;
            logger.debug("PublisherSession closed on topic: {}", this.topicName);
        }

        logger.info("Communication stopped successfully on Topic: '{}'", this.topicName);

        logger.debug(Constants.METHOD_ENDED);
    }

    @Override
    public synchronized void onException(JMSException ex) {
        logger.error(ExceptionUtils.getMessage(ex));
        logger.error(ExceptionUtils.getStackTrace(ex));
    }

    public String getTopic() {
        return this.topicName;
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