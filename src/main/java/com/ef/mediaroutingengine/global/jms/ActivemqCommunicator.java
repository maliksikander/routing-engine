package com.ef.mediaroutingengine.global.jms;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.CimEvent;
import com.ef.cim.objectmodel.CimEventName;
import com.ef.cim.objectmodel.CimEventType;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.AgentReservedDto;
import com.ef.cim.objectmodel.dto.NoAgentAvailableDto;
import com.ef.cim.objectmodel.dto.RevokeResourceDto;
import com.ef.cim.objectmodel.dto.TaskEnqueuedDto;
import com.ef.cim.objectmodel.dto.TaskEnqueuedQueue;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.dto.StateChangeEvent;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
     * The Topics.
     */
    private final List<String> topics = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * The Connection.
     */
    Connection connection;
    /**
     * The State Change Event Publisher.
     */
    MessageProducer stateChangeEventPublisher;
    /**
     * The Conversation event publisher.
     */
    MessageProducer conversationEventPublisher;
    /**
     * The State Change Event Publisher Session.
     */
    private Session stateChangeEventPublisherSession;
    /**
     * The Conversation Event Publisher Session.
     */
    private Session conversationEventPublisherSession;

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
            String messageStr = this.getSerializedCimEvent(AdapterUtility.createTaskDtoFrom(task),
                    CimEventName.TASK_STATE_CHANGED, task.getTopicId(), task.getChannelSession());
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
     * To publish NoAgentAvailable event.
     *
     * @param task the Task
     */
    @Override
    public void publishNoAgentAvailable(Task task) {
        try {
            String messageStr = this.getSerializedCimEvent(new NoAgentAvailableDto(task.getType()),
                    CimEventName.NO_AGENT_AVAILABLE, task.getTopicId(), task.getChannelSession());
            TextMessage messageToSend = this.conversationEventPublisherSession.createTextMessage();
            messageToSend.setText(messageStr);

            messageToSend.setJMSType(CimEventName.NO_AGENT_AVAILABLE.name());
            messageToSend.setJMSCorrelationID(MDC.get(Constants.MDC_CORRELATION_ID));

            conversationEventPublisher.send(messageToSend);

            logger.info("Jms event: '{}' with payload: '{}' published on topic: '{}'",
                    CimEventName.NO_AGENT_AVAILABLE, messageStr, topics.get(1));
        } catch (JMSException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Publish Task Enqueued Event.
     *
     * @param task  the Task
     * @param queue the queue info
     */
    @Override
    public void publishTaskEnqueued(Task task, PrecisionQueue queue) {
        try {
            TaskEnqueuedDto taskEnqueuedDto = new TaskEnqueuedDto(AdapterUtility.createTaskDtoFrom(task),
                    new TaskEnqueuedQueue(queue.getId(), queue.getName()));
            String messageStr = this.getSerializedCimEvent(taskEnqueuedDto,
                    CimEventName.TASK_ENQUEUED, task.getTopicId(), task.getChannelSession());
            TextMessage messageToSend = this.conversationEventPublisherSession.createTextMessage();
            messageToSend.setText(messageStr);

            messageToSend.setJMSType(CimEventName.TASK_ENQUEUED.name());
            messageToSend.setJMSCorrelationID(MDC.get(Constants.MDC_CORRELATION_ID));

            conversationEventPublisher.send(messageToSend);

            logger.info("Jms event: '{}' with payload: '{}' published on topic: '{}'",
                    CimEventName.TASK_ENQUEUED, messageStr, topics.get(1));
        } catch (JMSException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * to publish Agent Reserved event on topic.
     *
     * @param task the task object
     */
    @Override
    public void publishAgentReserved(Task task, CCUser agent) {
        try {
            String messageStr = this.getSerializedCimEvent(new AgentReservedDto(AdapterUtility
                    .createTaskDtoFrom(task), agent), CimEventName.AGENT_RESERVED, task.getTopicId(),
                    task.getChannelSession());
            TextMessage messageToSend = this.conversationEventPublisherSession.createTextMessage();
            messageToSend.setText(messageStr);

            messageToSend.setJMSType(CimEventName.AGENT_RESERVED.name());
            messageToSend.setJMSCorrelationID(MDC.get(Constants.MDC_CORRELATION_ID));

            conversationEventPublisher.send(messageToSend);

            logger.info("Jms event: '{}' with payload: '{}' published on topic: '{}'",
                    CimEventName.AGENT_RESERVED, messageStr, topics.get(1));
        } catch (JMSException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * to publish revoke resource on topic.
     *
     * @param task the task object.
     */
    @Override
    public void publishRevokeTask(Task task, RevokeResourceDto revokeResourceDto) {
        try {
            String messageStr = this.getSerializedCimEvent(revokeResourceDto, CimEventName.REVOKE_RESOURCE,
                    task.getTopicId(), task.getChannelSession());
            TextMessage messageToSend = this.conversationEventPublisherSession.createTextMessage();
            messageToSend.setText(messageStr);

            messageToSend.setJMSType(CimEventName.REVOKE_RESOURCE.name());
            messageToSend.setJMSCorrelationID(MDC.get(Constants.MDC_CORRELATION_ID));

            conversationEventPublisher.send(messageToSend);

            logger.info("Jms event: '{}' with payload: '{}' published on topic: '{}'",
                    CimEventName.REVOKE_RESOURCE, messageStr, topics.get(1));
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
    private String getSerializedCimEvent(Object message, CimEventName eventName, String conversationId,
                                         ChannelSession channelSession)
            throws JsonProcessingException {
        CimEvent cimEvent = new CimEvent(message, eventName, CimEventType.NOTIFICATION,
                conversationId, AdapterUtility.getSender(), channelSession, channelSession.getRoomId());
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