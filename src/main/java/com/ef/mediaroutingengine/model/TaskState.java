package com.ef.mediaroutingengine.model;

public class TaskState {
    private Enums.TaskStateName name;
    private Enums.TaskStateReasonCode reasonCode;

    public TaskState() {

    }

    public TaskState(Enums.TaskStateName name, Enums.TaskStateReasonCode reasonCode) {
        this.name = name;
        this.reasonCode = reasonCode;
    }

    public Enums.TaskStateName getName() {
        return name;
    }

    public void setName(Enums.TaskStateName name) {
        this.name = name;
    }

    public Enums.TaskStateReasonCode getReasonCode() {
        return reasonCode;
    }

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
