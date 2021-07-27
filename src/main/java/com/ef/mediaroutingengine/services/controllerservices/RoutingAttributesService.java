package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.RoutingAttributeDeleteConflictResponse;
import java.util.List;
import java.util.UUID;

/**
 * The interface Routing attributes service.
 */
public interface RoutingAttributesService {

    /**
     * Create routing attribute.
     *
     * @param routingAttribute the routing attribute
     * @return the routing attribute
     */
    RoutingAttribute create(RoutingAttribute routingAttribute);

    /**
     * Retrieve list.
     *
     * @return the list
     */
    List<RoutingAttribute> retrieve();

    /**
     * Update routing attribute.
     *
     * @param routingAttribute the routing attribute
     * @param id               the id
     * @return the routing attribute
     * @throws Exception the exception
     */
    RoutingAttribute update(RoutingAttribute routingAttribute, UUID id) throws Exception;

    /**
     * Delete routing attribute delete conflict response.
     *
     * @param id the id
     * @return the routing attribute delete conflict response
     */
    RoutingAttributeDeleteConflictResponse delete(UUID id);
}
