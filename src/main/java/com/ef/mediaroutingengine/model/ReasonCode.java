package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.commons.Enums;
import java.util.UUID;

public class ReasonCode {
    private final UUID id;
    private String name;
    private Enums.ReasonCodeType type;

    public ReasonCode() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Enums.ReasonCodeType getType() {
        return type;
    }

    public void setType(Enums.ReasonCodeType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ReasonCode{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", type=" + type
                + '}';
    }
}
