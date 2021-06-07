package com.ef.mediaroutingengine.model;

public class AgentState {
    Enums.AgentStateName name;
    Enums.AgentStateReasonCode reasonCode;

    public AgentState() {

    }

    public AgentState(Enums.AgentStateName name, Enums.AgentStateReasonCode reasonCode) {
        this.name = name;
        this.reasonCode = reasonCode;
    }

    public Enums.AgentStateName getName() {
        return name;
    }

    public void setName(Enums.AgentStateName name) {
        this.name = name;
    }

    public Enums.AgentStateReasonCode getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(Enums.AgentStateReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    @Override
    public String toString() {
        return "AgentState{"
                + "name=" + name
                + ", reasonCode=" + reasonCode
                + '}';
    }
}
