package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.CCUser;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AgentPresence {
    private CCUser agent;
    private AgentState state;
    private Timestamp stateChangeTime;
    private List<AgentMrdState> agentMrdStates;

    /**
     * Default constructor.
     */
    public AgentPresence() {
        this.agentMrdStates = new ArrayList<>();
    }

    /**
     * Parameterized Constructor.
     * @param agent agent object
     * @param state state of the agent
     * @param agentMrdStates list of mrd states of the agent.
     */
    public AgentPresence(CCUser agent, AgentState state, List<AgentMrdState> agentMrdStates) {
        this.agent = agent;
        this.state = state;
        this.stateChangeTime = new Timestamp(System.currentTimeMillis());
        this.agentMrdStates = agentMrdStates;
    }

    public CCUser getAgent() {
        return agent;
    }

    public void setAgent(CCUser agent) {
        this.agent = agent;
    }

    public AgentState getState() {
        return state;
    }

    public void setState(AgentState state) {
        this.state = state;
    }

    public Timestamp getStateChangeTime() {
        return stateChangeTime;
    }

    public void setStateChangeTime(Timestamp stateChangeTime) {
        this.stateChangeTime = stateChangeTime;
    }

    public List<AgentMrdState> getAgentMrdStates() {
        return agentMrdStates;
    }

    public void setAgentMrdStates(List<AgentMrdState> agentMrdStates) {
        this.agentMrdStates = agentMrdStates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentPresence that = (AgentPresence) o;
        return Objects.equals(agent, that.agent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agent);
    }

    @Override
    public String toString() {
        return "AgentPresence{"
                + "agent=" + agent
                + ", state=" + state
                + ", stateChangeTime=" + stateChangeTime
                + ", agentMrdStates=" + agentMrdStates
                + '}';
    }
}
