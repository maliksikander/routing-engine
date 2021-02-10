package com.ef.mediaroutingengine.repositories;

import com.ef.cim.objectmodel.CCUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AgentsRepository extends MongoRepository<CCUser, UUID> {
    @Query("{'associatedRoutingAttributes.routingAttribute.id': ?0}")
    List<CCUser> findByRoutingAttributeId(UUID id);
}
