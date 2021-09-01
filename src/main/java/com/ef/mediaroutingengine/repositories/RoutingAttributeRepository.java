package com.ef.mediaroutingengine.repositories;

import com.ef.cim.objectmodel.RoutingAttribute;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The interface Routing attribute repository.
 */
public interface RoutingAttributeRepository extends MongoRepository<RoutingAttribute, String> {

}
