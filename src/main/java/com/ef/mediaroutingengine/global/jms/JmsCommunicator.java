package com.ef.mediaroutingengine.global.jms;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.Serializable;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

/**
 * This interface describes a message communication service to be used
 * by the bot framework in CIM to communicate over a JMS broker.
 *
 * <p>It provides an init() method to initialize a connection with an JMS broker,
 * a single topic subscriber and a single topic publisher on that connection. This way
 * messages on a particular topic can be received and published.
 *
 * <p>It extends the MessageListener interface to listen to messages using the onMessage()
 * method and provides publish() method to send messages on topic.
 *
 * <p>It also provides a stop() method to stop the connection gracefully.
 */
public interface JmsCommunicator extends ExceptionListener {
    /**
     * To initialize a connection and a topic subscriber and publisher
     * on that connection.
     *
     * @param stateChangeTopic the state change topic
     * @param customerTopic    the customer topic
     * @throws JMSException the jms exception
     */
    void init(String stateChangeTopic, String customerTopic) throws JMSException;

    /**
     * To stop the connection, if it has been initialized, gracefully.
     *
     * @throws JMSException exception
     */
    void stop() throws JMSException;

    /**
     * Publish.
     *
     * @param message   the message
     * @param eventName the event name
     * @throws JMSException            the jms exception
     * @throws JsonProcessingException the json processing exception
     */
    void publish(Serializable message, Enums.JmsEventName eventName) throws JMSException, JsonProcessingException;

    /**
     * Publish task state changed.
     *
     * @param task              the task
     * @param channelSession    the channel session
     * @param taskStateChanged  the task state changed
     * @param mediaStateChanges the media state changes
     */
    void publishTaskStateChanged(Task task, ChannelSession channelSession, boolean taskStateChanged,
                                 String... mediaStateChanges);

    /**
     * Publish NoAgentAvailable event.
     *
     * @param conversationId the conversation id
     * @param media          the media
     */
    void publishNoAgentAvailable(String conversationId, TaskMedia media);

    /**
     * Publish Task enqueue event.
     *
     * @param task  the task
     * @param media the media
     * @param queue precision queue
     */
    void publishTaskEnqueued(Task task, TaskMedia media, PrecisionQueue queue);

    /**
     * Publish Agent Reserved event.
     *
     * @param task  the task object
     * @param media the media
     * @param agent the agent
     */
    void publishAgentReserved(Task task, TaskMedia media, CCUser agent);
}
