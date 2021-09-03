package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.ChannelSession;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public class PullAssignTaskRequest {
    @NotNull
    private UUID agent;
    @NotNull
    private ChannelSession channelSession;

    public PullAssignTaskRequest() {

    }

    public PullAssignTaskRequest(UUID agent, ChannelSession channelSession) {
        this.agent = agent;
        this.channelSession = channelSession;
    }

    public UUID getAgent() {
        return agent;
    }

    public void setAgent(UUID agent) {
        this.agent = agent;
    }

    public ChannelSession getChannelSession() {
        return channelSession;
    }

    public void setChannelSession(ChannelSession channelSession) {
        this.channelSession = channelSession;
    }
}
