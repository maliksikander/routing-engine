package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ChannelSession;
import java.util.UUID;

public class AssignTaskRequest {

    private ChannelSession channelSession;
    private CCUser ccUser;
    private UUID topicId;
    private UUID taskId;

    public ChannelSession getChannelSession() {
        return channelSession;
    }

    public void setChannelSession(ChannelSession channelSession) {
        this.channelSession = channelSession;
    }

    public CCUser getCcUser() {
        return ccUser;
    }

    public void setCcUser(CCUser ccUser) {
        this.ccUser = ccUser;
    }

    public UUID getTopicId() {
        return topicId;
    }

    public void setTopicId(UUID topicId) {
        this.topicId = topicId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }
}
