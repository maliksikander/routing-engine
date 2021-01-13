package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.ChannelSession;

public class AssignResourceRequest {
    String topicId;
    ChannelSession channelSession;

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public ChannelSession getChannelSession() {
        return channelSession;
    }

    public void setChannelSession(ChannelSession channelSession) {
        this.channelSession = channelSession;
    }
}
