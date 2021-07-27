package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * The interface Precision queue entity repository.
 */
public interface PrecisionQueueEntityRepository extends MongoRepository<PrecisionQueueEntity, UUID> {

    /**
     * Find by mrd id list.
     *
     * @param id the id
     * @return the list
     */
    @Query("{'mrd.id': ?0}")
    List<PrecisionQueueEntity> findByMrdId(UUID id);

    /**
     * Find by routing attribute id list.
     *
     * @param id the id
     * @return the list
     */
    @Query("{'steps.expressions.terms.routingAttribute.id': ?0}")
    List<PrecisionQueueEntity> findByRoutingAttributeId(UUID id);
}
