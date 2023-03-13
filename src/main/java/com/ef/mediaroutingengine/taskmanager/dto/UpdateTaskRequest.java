package com.ef.mediaroutingengine.taskmanager.dto;

import com.ef.cim.objectmodel.ChannelSession;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Update task request.
 */
@Getter
@Setter
@ToString
public class UpdateTaskRequest {
    /**
     * The Channel session.
     */
    private ChannelSession channelSession;
}
