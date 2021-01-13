package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.model.Task;

public class AssignTaskRequest {
    private ChannelSession channelSession;
    private CCUser ccUser;
    private Task task;
    private String topicId;

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

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }
}
