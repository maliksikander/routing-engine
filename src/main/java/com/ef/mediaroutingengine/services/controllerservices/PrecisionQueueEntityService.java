package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import java.util.List;
import java.util.UUID;

/**
 * The interface Precision queue entity service.
 */
public interface PrecisionQueueEntityService {

    /**
     * Create precision queue entity.
     *
     * @param precisionQueueEntity the precision queue entity
     * @return the precision queue entity
     * @throws Exception the exception
     */
    PrecisionQueueEntity create(PrecisionQueueEntity precisionQueueEntity) throws Exception;

    /**
     * Retrieve list.
     *
     * @return the list
     */
    List<PrecisionQueueEntity> retrieve();

    /**
     * Update precision queue entity.
     *
     * @param precisionQueueEntity the precision queue entity
     * @param id                   the id
     * @return the precision queue entity
     * @throws Exception the exception
     */
    PrecisionQueueEntity update(PrecisionQueueEntity precisionQueueEntity, UUID id) throws Exception;

    /**
     * Delete.
     *
     * @param id the id
     * @throws Exception the exception
     */
    void delete(UUID id) throws Exception;
}
