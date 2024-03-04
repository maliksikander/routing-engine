package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.routing.dto.RoutingAttributeDeleteConflictResponse;
import java.util.List;
import java.util.Set;

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
     */
    RoutingAttribute update(RoutingAttribute routingAttribute, String id);

    /**
     * Delete routing attribute delete conflict response.
     *
     * @param id the id
     * @return the routing attribute delete conflict response
     */
    RoutingAttributeDeleteConflictResponse delete(String id);

    /**
     * This function retrieves all agents with the associated routing attributes.
     *
     * @param routingAttributes routingAttributes list.
     * @return Agents List
     */
    Set<CCUser> retrieveAgentsWithAssociatedRoutingAttributes(List<RoutingAttribute> routingAttributes);
}
