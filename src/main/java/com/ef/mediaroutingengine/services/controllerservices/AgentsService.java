package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.CCUser;

import java.util.List;
import java.util.UUID;

public interface AgentsService {
    CCUser create(CCUser agent);
    List<CCUser> retrieve();
    CCUser update(CCUser agent, UUID id);
    void delete(UUID id);
}
