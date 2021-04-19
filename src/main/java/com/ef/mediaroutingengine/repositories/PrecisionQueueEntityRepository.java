package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PrecisionQueueEntityRepository extends MongoRepository<PrecisionQueueEntity, UUID> {

    @Query("{'mrd.id': ?0}")
    List<PrecisionQueueEntity> findByMrdId(UUID id);

    @Query("{'steps.expressions.terms.routingAttribute.id': ?0}")
    List<PrecisionQueueEntity> findByRoutingAttributeId(UUID id);
}
