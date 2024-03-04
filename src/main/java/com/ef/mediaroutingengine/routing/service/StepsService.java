package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.StepEntity;
import java.util.Optional;
import java.util.Set;
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

    /**
     * This function retrieves all the agents that are falling in the steps criteria in queue X.
     *
     * @param stepId  stepId
     * @param queueId queueId
     * @return list og agents.
     */
    Set<CCUser> previewAgentsMatchingStepCriteriaInQueue(String queueId, Optional<String> stepId);
}
