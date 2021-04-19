package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.CCUser;
import java.util.List;
import java.util.UUID;

public interface AgentsService {

    CCUser create(CCUser agent) throws Exception;

    List<CCUser> retrieve();

    CCUser update(CCUser agent, UUID id) throws Exception;

    void delete(UUID id) throws Exception;
}
