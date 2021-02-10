package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MediaRoutingDomainRepository extends MongoRepository<MediaRoutingDomain, UUID> {
    MediaRoutingDomain findByName(String name);
    //MediaRoutingDomain findById(UUID id);
    boolean existsById(UUID id);
}
