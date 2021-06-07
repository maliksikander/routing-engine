package com.ef.mediaroutingengine.model;

public class MrdState {
    Enums.AgentMrdStateName name;
    Enums.AgentMrdStateReasonCode reasonCode;

    public MrdState() {

    }

    public MrdState(Enums.AgentMrdStateName name, Enums.AgentMrdStateReasonCode reasonCode) {
        this.name = name;
        this.reasonCode = reasonCode;
    }

    public Enums.AgentMrdStateName getName() {
        return name;
    }

    public void setName(Enums.AgentMrdStateName name) {
        this.name = name;
    }

    public Enums.AgentMrdStateReasonCode getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(Enums.AgentMrdStateReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    @Override
    public String toString() {
        return "MrdState{"
                + "name=" + name
                + ", reasonCode=" + reasonCode
                + '}';
    }
}
