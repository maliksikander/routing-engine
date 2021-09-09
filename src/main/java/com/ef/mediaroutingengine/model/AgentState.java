package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.commons.Enums;
import javax.validation.constraints.NotNull;

/**
 * The type Agent state.
 */
public class AgentState {
    /**
     * The Name.
     */
    @NotNull
    private Enums.AgentStateName name;
    /**
     * The Reason code.
     */
    private ReasonCode reasonCode;

    /**
     * Instantiates a new Agent state.
     */
    public AgentState() {

    }

    /**
     * Instantiates a new Agent state.
     *
     * @param name       the name
     * @param reasonCode the reason code
     */
    public AgentState(Enums.AgentStateName name, ReasonCode reasonCode) {
        this.name = name;
        this.reasonCode = reasonCode;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public Enums.AgentStateName getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(Enums.AgentStateName name) {
        this.name = name;
    }

    /**
     * Gets reason code.
     *
     * @return the reason code
     */
    public ReasonCode getReasonCode() {
        return reasonCode;
    }

    /**
     * Sets reason code.
     *
     * @param reasonCode the reason code
     */
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
