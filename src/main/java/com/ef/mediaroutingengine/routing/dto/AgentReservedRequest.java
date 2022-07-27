package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.CCUser;
import java.util.UUID;

/**
 * An AgentReservedRequest object is used to call the Bot-Frameworks's Agent-Reserved API as Request Body.
 */
public class AgentReservedRequest {
    /**
     * ID of the JMS-topic on which an Agent is reserved.
     */
    UUID topicId;
    /**
     * Agent that has been reserved for this topic.
     */
    CCUser agent;

    /**
     * Gets topic id.
     *
     * @return the topic id
     */
    public UUID getTopicId() {
        return topicId;
    }

    /**
     * Sets topic id.
     *
     * @param topicId the topic id
     */
    public void setTopicId(UUID topicId) {
        this.topicId = topicId;
    }

    /**
     * Gets agent.
     *
     * @return the agent
     */
    public CCUser getAgent() {
        return agent;
    }

    /**
     * Sets agent.
     *
     * @param agent the agent
     */
    public void setAgent(CCUser agent) {
        this.agent = agent;
    }
}
