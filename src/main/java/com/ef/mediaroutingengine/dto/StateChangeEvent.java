package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.Enums;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The type State change event.
 */
public class StateChangeEvent implements Serializable {
    /**
     * The Name.
     */
    private Enums.JmsEventName name;
    /**
     * The Data.
     */
    private Serializable data;
    /**
     * The Timestamp.
     */
    private Timestamp timestamp;
    /**
     * The Topic id.
     */
    private String topicId;

    /**
     * Instantiates a new State change event.
     */
    public StateChangeEvent() {

    }

    /**
     * Parametrized Constructor.
     *
     * @param name    event name
     * @param data    event data
     * @param topicId the topic id
     */
    public StateChangeEvent(Enums.JmsEventName name, Serializable data, String topicId) {
        this.name = name;
        this.data = data;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.topicId = topicId;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public Enums.JmsEventName getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(Enums.JmsEventName name) {
        this.name = name;
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public Serializable getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data the data
     */
    public void setData(Serializable data) {
        this.data = data;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets timestamp.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets topic id.
     *
     * @return the topic id
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * Sets topic id.
     *
     * @param topicId the topic id
     */
    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    @Override
    public String toString() {
        return "StateChangeEvent{"
                + "name=" + name
                + ", data=" + data
                + ", timestamp=" + timestamp
                + ", topicId=" + topicId
                + '}';
    }
}
