package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.commons.Enums;

public class AgentState {
    Enums.AgentStateName name;
    ReasonCode reasonCode;

    public AgentState() {

    }

    public AgentState(Enums.AgentStateName name, ReasonCode reasonCode) {
        this.name = name;
        this.reasonCode = reasonCode;
    }

    public Enums.AgentStateName getName() {
        return name;
    }

    public void setName(Enums.AgentStateName name) {
        this.name = name;
    }

    public ReasonCode getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(ReasonCode reasonCode) {
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
