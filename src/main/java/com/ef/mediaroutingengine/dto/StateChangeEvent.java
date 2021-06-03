package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.Enums;
import java.io.Serializable;
import java.sql.Timestamp;

public class StateChangeEvent implements Serializable {
    private Enums.RedisEventName name;
    private Serializable data;
    private Timestamp timestamp;

    public StateChangeEvent() {

    }

    /**
     * Parametrized Constructor.
     *
     * @param name event name
     * @param data event data
     */
    public StateChangeEvent(Enums.RedisEventName name, Serializable data) {
        this.name = name;
        this.data = data;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Enums.RedisEventName getName() {
        return name;
    }

    public void setName(Enums.RedisEventName name) {
        this.name = name;
    }

    public Serializable getData() {
        return data;
    }

    public void setData(Serializable data) {
        this.data = data;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "StateChangeEvent{"
                + "name=" + name
                + ", data=" + data
                + ", timestamp=" + timestamp
                + '}';
    }
}
