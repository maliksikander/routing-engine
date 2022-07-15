package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.dto.AssignResourceRequest;

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
    String assign(AssignResourceRequest request, boolean useQueueName);
}
