package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.CCUser;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AgentPresence {
    private CCUser agent;
    private String state;
    private Timestamp stateChangeTime;
    private List<String> topics;
    private List<AgentMrdState> agentMrdStates;

    public AgentPresence() {
        this.topics = new ArrayList<>();
    }

    public CCUser getAgent() {
        return agent;
    }

    public void setAgent(CCUser agent) {
        this.agent = agent;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getStateChangeTime() {
        return stateChangeTime;
    }

    public void setStateChangeTime(Timestamp stateChangeTime) {
        this.stateChangeTime = stateChangeTime;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public boolean containsTopic(String topic) {
        return this.topics.contains(topic);
    }

    public boolean addTopic(String topic) {
        return this.topics.add(topic);
    }

    public boolean removeTopic(String topic) {
        return this.topics.remove(topic);
    }

    public List<AgentMrdState> getAgentMrdStates() {
        return agentMrdStates;
    }

    public void setAgentMrdStates(List<AgentMrdState> agentMrdStates) {
        this.agentMrdStates = agentMrdStates;
    }
}
