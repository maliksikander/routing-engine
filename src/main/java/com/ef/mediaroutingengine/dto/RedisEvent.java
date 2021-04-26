package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.CommonEnums;
import java.io.Serializable;

public class RedisEvent implements Serializable {
    CommonEnums.RedisEventName name;
    Object data;

    public CommonEnums.RedisEventName getName() {
        return name;
    }

    public void setName(CommonEnums.RedisEventName name) {
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
