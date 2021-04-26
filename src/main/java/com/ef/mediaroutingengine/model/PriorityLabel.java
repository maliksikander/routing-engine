package com.ef.mediaroutingengine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PriorityLabel {
    @JsonProperty
    private String name;
    @JsonProperty
    private int priority;

    public String getName() {
        return name;
    }

    public void setName(String labelName) {
        this.name = labelName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Name: [" + this.getName() + "], Priority: [" + this.getPriority() + "]";
    }
}
