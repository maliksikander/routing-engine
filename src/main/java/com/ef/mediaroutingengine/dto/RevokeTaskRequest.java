package com.ef.mediaroutingengine.dto;

import java.util.UUID;

/**
 * The type Revoke task request.
 */
public class RevokeTaskRequest {
    /**
     * The Task id.
     */
    private UUID taskId;
    /**
     * The Agent id.
     */
    private UUID agentId;

    private UUID topicId;

    public RevokeTaskRequest() {

    }

    /**
     * Instantiates a new Revoke task request.
     *
     * @param taskId  the task id
     * @param agentId the agent id
     * @param topicId the topic id
     */
    public RevokeTaskRequest(UUID taskId, UUID agentId, UUID topicId) {
        this.taskId = taskId;
        this.agentId = agentId;
        this.topicId = topicId;
    }

    /**
     * Gets task id.
     *
     * @return the task id
     */
    public UUID getTaskId() {
        return taskId;
    }

    /**
     * Sets task id.
     *
     * @param taskId the task id
     */
    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    /**
     * Gets agent id.
     *
     * @return the agent id
     */
    public UUID getAgentId() {
        return agentId;
    }

    /**
     * Sets agent id.
     *
     * @param agentId the agent id
     */
    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }

    public UUID getTopicId() {
        return topicId;
    }

    public void setTopicId(UUID topicId) {
        this.topicId = topicId;
    }
}
