package com.ef.mediaroutingengine.routing.service;

import com.ef.mediaroutingengine.routing.dto.AssignResourceRequest;
/**
 * The interface Assign resource service.
 */
public interface AssignResourceService {

    /**
     * Assign string.
     *
     * @param request the request
     * @return the string
     */
    String assign(AssignResourceRequest request, boolean useQueueName, boolean offerToAgent, int priority);
}
