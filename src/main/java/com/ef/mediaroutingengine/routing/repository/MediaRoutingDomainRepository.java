package com.ef.mediaroutingengine.routing.repository;

import com.ef.cim.objectmodel.MediaRoutingDomain;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Media routing domain repository.
 */
@Repository
public interface MediaRoutingDomainRepository extends MongoRepository<MediaRoutingDomain, String> {

}
