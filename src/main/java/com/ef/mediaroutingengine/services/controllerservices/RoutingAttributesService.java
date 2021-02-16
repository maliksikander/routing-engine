package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.RoutingAttributeDeleteConflictResponse;

import java.util.List;
import java.util.UUID;

public interface RoutingAttributesService {
    RoutingAttribute create(RoutingAttribute routingAttribute);
    List<RoutingAttribute> retrieve();
    RoutingAttribute update(RoutingAttribute routingAttribute, UUID id);
    RoutingAttributeDeleteConflictResponse delete(UUID id);
}