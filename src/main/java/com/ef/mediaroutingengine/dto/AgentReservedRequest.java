package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.CCUser;
import java.util.UUID;

public class AgentReservedRequest {

    UUID topicId;
    CCUser agent;

    public UUID getTopicId() {
        return topicId;
    }

    public void setTopicId(UUID topicId) {
        this.topicId = topicId;
    }

    public CCUser getAgent() {
        return agent;
    }

    public void setAgent(CCUser agent) {
        this.agent = agent;
    }
}
