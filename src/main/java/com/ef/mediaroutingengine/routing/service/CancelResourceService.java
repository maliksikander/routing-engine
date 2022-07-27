package com.ef.mediaroutingengine.routing.service;

import com.ef.mediaroutingengine.routing.dto.CancelResourceRequest;

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
