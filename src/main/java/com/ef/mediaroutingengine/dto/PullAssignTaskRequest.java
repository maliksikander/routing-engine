package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.ChannelSession;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public class PullAssignTaskRequest {
    @NotNull
    private UUID agentId;
    @NotNull
    private ChannelSession channelSession;

    public PullAssignTaskRequest() {

    }

    public PullAssignTaskRequest(UUID agentId, ChannelSession channelSession) {
        this.agentId = agentId;
        this.channelSession = channelSession;
    }

    public UUID getAgentId() {
        return agentId;
    }

    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }

    public ChannelSession getChannelSession() {
        return channelSession;
    }

    public void setChannelSession(ChannelSession channelSession) {
        this.channelSession = channelSession;
    }
}
