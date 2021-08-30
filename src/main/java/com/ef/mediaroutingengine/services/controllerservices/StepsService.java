package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.StepEntity;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

/**
 * The interface Steps service.
 */
public interface StepsService {
    /**
     * Create precision queue entity.
     *
     * @param queueId    the queue id
     * @param stepEntity the step entity
     * @return the precision queue entity
     */
    PrecisionQueueEntity create(UUID queueId, StepEntity stepEntity);

    /**
     * Update precision queue entity.
     *
     * @param id         the id
     * @param queueId    the queue id
     * @param stepEntity the step entity
     * @return the precision queue entity
     */
    PrecisionQueueEntity update(UUID id, UUID queueId, StepEntity stepEntity);

    /**
     * Delete response entity.
     *
     * @param queueId the queue id
     * @param id      the id
     * @return the response entity
     */
    ResponseEntity<Object> delete(UUID queueId, UUID id);
}
