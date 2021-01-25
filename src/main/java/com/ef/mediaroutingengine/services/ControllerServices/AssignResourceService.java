package com.ef.mediaroutingengine.services.ControllerServices;

import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface AssignResourceService {
    void assign(AssignResourceRequest request);
}
