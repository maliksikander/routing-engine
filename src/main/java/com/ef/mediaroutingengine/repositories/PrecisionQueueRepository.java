package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * The interface Precision queue entity repository.
 */
public interface PrecisionQueueRepository extends MongoRepository<PrecisionQueueEntity, String> {

    /**
     * Find by mrd id list.
     *
     * @param id the id
     * @return the list
     */
    @Query("{'mrd.id': ?0}")
    List<PrecisionQueueEntity> findByMrdId(String id);

    /**
     * Find by routing attribute id list.
     *
     * @param id the id
     * @return the list
     */
    @Query("{'steps.expressions.terms.routingAttribute.id': ?0}")
    List<PrecisionQueueEntity> findByRoutingAttributeId(String id);
}
