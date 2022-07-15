package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.ChannelSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * An AssignResourceRequest object is used by the assign-resource API as Request Body.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
}
