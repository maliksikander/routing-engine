package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ChannelSession;
import java.util.UUID;

/**
 * The type Assign task request.
 */
public class AssignTaskRequest {

    /**
     * The Channel session.
     */
    private ChannelSession channelSession;
    /**
     * The Cc user.
     */
    private CCUser ccUser;
    /**
     * The Topic id.
     */
    private UUID conversationId;
    /**
     * The Task id.
     */
    private UUID taskId;

    /**
     * Gets channel session.
     *
     * @return the channel session
     */
    public ChannelSession getChannelSession() {
        return channelSession;
    }

    /**
     * Sets channel session.
     *
     * @param channelSession the channel session
     */
    public void setChannelSession(ChannelSession channelSession) {
        this.channelSession = channelSession;
    }

    /**
     * Gets cc user.
     *
     * @return the cc user
     */
    public CCUser getCcUser() {
        return ccUser;
    }

    /**
     * Sets cc user.
     *
     * @param ccUser the cc user
     */
    public void setCcUser(CCUser ccUser) {
        this.ccUser = ccUser;
    }

    /**
     * Gets topic id.
     *
     * @return the topic id
     */
    public UUID getConversationId() {
        return conversationId;
    }

    /**
     * Sets topic id.
     *
     * @param conversationId the topic id
     */
    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
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
}
