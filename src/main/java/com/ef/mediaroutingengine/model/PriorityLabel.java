package com.ef.mediaroutingengine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The type Priority label.
 */
public class PriorityLabel {
    /**
     * The Name.
     */
    @JsonProperty
    private String name;
    /**
     * The Priority.
     */
    @JsonProperty
    private int priority;

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param labelName the label name
     */
    public void setName(String labelName) {
        this.name = labelName;
    }

    /**
     * Gets priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets priority.
     *
     * @param priority the priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Name: [" + this.getName() + "], Priority: [" + this.getPriority() + "]";
    }
}
