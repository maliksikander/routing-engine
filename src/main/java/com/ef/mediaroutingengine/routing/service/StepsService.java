package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.StepEntity;
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
    PrecisionQueueEntity create(String queueId, StepEntity stepEntity);

    /**
     * Update precision queue entity.
     *
     * @param id         the id
     * @param queueId    the queue id
     * @param stepEntity the step entity
     * @return the precision queue entity
     */
    PrecisionQueueEntity update(String id, String queueId, StepEntity stepEntity);

    /**
     * Delete response entity.
     *
     * @param queueId the queue id
     * @param id      the id
     * @return the response entity
     */
    ResponseEntity<Object> delete(String queueId, String id);
}
