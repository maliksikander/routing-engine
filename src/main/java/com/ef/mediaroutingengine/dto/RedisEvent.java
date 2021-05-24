package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.Enums;
import java.io.Serializable;

public class RedisEvent implements Serializable {
    Enums.RedisEventName name;
    Object data;

    public Enums.RedisEventName getName() {
        return name;
    }

    public void setName(Enums.RedisEventName name) {
        this.name = name;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RedisEvent{"
                + "name=" + name
                + ", data=" + data
                + '}';
    }
}
