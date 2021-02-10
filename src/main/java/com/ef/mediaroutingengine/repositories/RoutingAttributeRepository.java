package com.ef.mediaroutingengine.repositories;

import com.ef.cim.objectmodel.RoutingAttribute;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface RoutingAttributeRepository extends MongoRepository<RoutingAttribute, UUID> {
}
