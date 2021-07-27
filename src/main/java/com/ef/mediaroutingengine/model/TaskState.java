package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.commons.Enums;
import java.io.Serializable;

/**
 * The type Task state.
 */
public class TaskState implements Serializable {
    /**
     * The Name.
     */
    private Enums.TaskStateName name;
    /**
     * The Reason code.
     */
    private Enums.TaskStateReasonCode reasonCode;

    /**
     * Instantiates a new Task state.
     */
    public TaskState() {

    }

    /**
     * Instantiates a new Task state.
     *
     * @param name       the name
     * @param reasonCode the reason code
     */
    public TaskState(Enums.TaskStateName name, Enums.TaskStateReasonCode reasonCode) {
        this.name = name;
        this.reasonCode = reasonCode;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public Enums.TaskStateName getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(Enums.TaskStateName name) {
        this.name = name;
    }

    /**
     * Gets reason code.
     *
     * @return the reason code
     */
    public Enums.TaskStateReasonCode getReasonCode() {
        return reasonCode;
    }

    /**
     * Sets reason code.
     *
     * @param reasonCode the reason code
     */
    public void setReasonCode(Enums.TaskStateReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    @Override
    public String toString() {
        return "TaskState{"
                + "name=" + name
                + ", reasonCode='" + reasonCode + '\''
                + '}';
    }
}
