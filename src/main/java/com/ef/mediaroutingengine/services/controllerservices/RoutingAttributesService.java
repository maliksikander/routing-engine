package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.RoutingAttribute;

import java.util.List;
import java.util.UUID;

public interface RoutingAttributesService {
    RoutingAttribute create(RoutingAttribute routingAttribute);
    List<RoutingAttribute> retrieve();
    void update(RoutingAttribute routingAttribute, UUID id);
    void delete(UUID id);
}
