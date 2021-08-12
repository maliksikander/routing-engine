package com.ef.mediaroutingengine.services.jms;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.Serializable;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageListener;

/**
 * This interface describes a message communication service to be used
 * by the bot framework in CIM to communicate over a JMS broker.
 *
 * <p>It provides an init() method to initialize a connection with an JMS broker,
 * a single topic subscriber and a single topic publisher on that connection. This way
 * messages on a particular topic can be received and published.
 *
 * <p>It extends the MessageListener interface to listen to messages using the onMessage()
 * method and provides a publish() method to send messages on topic.
 *
 * <p>It also provides a stop() method to stop the connection gracefully.
 */
public interface JmsCommunicator extends MessageListener, ExceptionListener {
    /**
     * To initialize a connection and a topic subscriber and publisher
     * on that connection.
     *
     * @param topic Topic name/id to subscribe to or publish messages to.
     * @throws JMSException exception
     */
    void init(String topic) throws JMSException;

    /**
     * To stop the connection, if it has been initialized, gracefully.
     *
     * @throws JMSException exception
     */
    void stop() throws JMSException;

    /**
     * Gets topic.
     *
     * @return the topic
     */
    String getTopic();

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
}
