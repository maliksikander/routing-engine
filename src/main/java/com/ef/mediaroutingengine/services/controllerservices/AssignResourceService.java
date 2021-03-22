package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.dto.AssignResourceRequest;

public interface AssignResourceService {

    void assign(AssignResourceRequest request);

    /**
     * Mocking assign agent for testing.
     *
     * @param request assign resource request.
     */
    void assignResource(AssignResourceRequest request);
}
