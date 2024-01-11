package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;

/**
 * The interface Assign resource service.
 */
public interface AssignResourceService {

    /**
     * Assign string.
     *
     * @param request the request
     * @param queue   the queue
     */
    void assign(String conversationId, AssignResourceRequest request, PrecisionQueue queue);
}
