package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.dto.CancelResourceRequest;

/**
 * The interface End task service.
 */
public interface CancelResourceService {
    /**
     * Cancel resource.
     *
     * @param request the request
     */
    void cancelResource(CancelResourceRequest request);
}
