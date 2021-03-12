package com.ef.mediaroutingengine.repositories;

import com.ef.cim.objectmodel.RoutingAttribute;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoutingAttributeRepository extends MongoRepository<RoutingAttribute, UUID> {

}
