package com.ef.mediaroutingengine.global.jms;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.TaskEnqueuedDto;
import com.ef.cim.objectmodel.dto.TaskEnqueuedQueue;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.model.Queue;
import com.ef.mediaroutingengine.taskmanager.model.Task;
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
     * Publish task state change for reporting.
     *
     * @param task the task
     */
    void publishTaskStateChangeForReporting(Task task);

    /**
     * Publish NoAgentAvailable event.
     *
     * @param task the task
     */
    void publishNoAgentAvailable(Task task);

    /**
     * Publish Task enqueue event.
     *
     * @param task the task
     * @param queue precision queue
     */
    void publishTaskEnqueued(Task task, PrecisionQueue queue);
}
