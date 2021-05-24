package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.ChannelSession;
import java.util.UUID;

public class AssignResourceRequest {
    private ChannelSession channelSession;
    private UUID queue;

    public ChannelSession getChannelSession() {
        return channelSession;
    }

    public void setChannelSession(ChannelSession channelSession) {
        this.channelSession = channelSession;
    }

    public UUID getQueue() {
        return queue;
    }

    public void setQueue(UUID queue) {
        this.queue = queue;
    }

    @Override
    public String toString() {
        return "AssignResourceRequest{"
                + "channelSession=" + channelSession
                + ", queue=" + queue
                + '}';
    }
}
