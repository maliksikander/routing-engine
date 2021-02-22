package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.model.PrecisionQueue;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PrecisionQueueRepository extends MongoRepository<PrecisionQueue, UUID> {

    @Query("{'mrd.id': ?0}")
    List<PrecisionQueue> findByMrdId(UUID id);

    @Query("{'steps.expressions.terms.routingAttribute.id': ?0}")
    List<PrecisionQueue> findByRoutingAttributeId(UUID id);
}
