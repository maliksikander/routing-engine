package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;

/**
 * The interface Pull assign task service.
 */
public interface PullAssignTaskService {
    /**
     * Assign task task dto.
     *
     * @param agent          the agent
     * @param mrd            the mrd
     * @param channelSession the channel session
     * @return the task dto
     */
    TaskDto assignTask(Agent agent, MediaRoutingDomain mrd, ChannelSession channelSession);
}
