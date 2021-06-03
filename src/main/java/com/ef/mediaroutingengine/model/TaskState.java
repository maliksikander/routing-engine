package com.ef.mediaroutingengine.model;

public class TaskState {
    private Enums.TaskStateName name;
    private String reasonCode;

    public TaskState() {

    }

    public TaskState(Enums.TaskStateName name, String reasonCode) {
        this.name = name;
        this.reasonCode = reasonCode;
    }

    public Enums.TaskStateName getName() {
        return name;
    }

    public void setName(Enums.TaskStateName name) {
        this.name = name;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    @Override
    public String toString() {
        return "TaskState{"
                + "name=" + name
                + ", reasonCode='"
                + reasonCode + '\''
                + '}';
    }
}
