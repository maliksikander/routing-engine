package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.CCUser;

public class AgentReservedRequest {
    String topicId;
    CCUser agent;

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public CCUser getAgent() {
        return agent;
    }

    public void setAgent(CCUser agent) {
        this.agent = agent;
    }
}
