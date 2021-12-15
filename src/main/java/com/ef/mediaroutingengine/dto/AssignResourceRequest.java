package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.ChannelSession;

/**
 * An AssignResourceRequest object is used by the
 * {@link com.ef.mediaroutingengine.controllers.AssignResourceController#assignResource(AssignResourceRequest)
 * Assign-Resource}*** API as Request Body.
 */
public class AssignResourceRequest {
    /**
     * Contains the channel configurations, routing-policy, default-queue, MRD e.t.c
     */
    private ChannelSession channelSession;
    /**
     * Requested queue to direct the request to. If this queue is null or not found, then the default queue
     * in the channel-session will be used.
     */
    private String queue;

    public AssignResourceRequest() {

    }

    public AssignResourceRequest(ChannelSession channelSession, String queue) {
        this.channelSession = channelSession;
        this.queue = queue;
    }

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
     * Gets queue.
     *
     * @return the queue
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Sets queue.
     *
     * @param queue the queue
     */
    public void setQueue(String queue) {
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
