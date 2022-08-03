package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ChannelSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Assign task request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
    private String conversationId;
    /**
     * The Task id.
     */
    private String taskId;
}
