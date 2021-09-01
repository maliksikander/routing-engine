package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.ExpressionEntity;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Step;
import com.ef.mediaroutingengine.model.StepEntity;
import com.ef.mediaroutingengine.model.TermEntity;
import com.ef.mediaroutingengine.repositories.PrecisionQueueRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.RoutingAttributesPool;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * The type Steps service.
 */
@Service
public class StepsServiceImpl implements StepsService {
    /**
     * The Repository.
     */
    private final PrecisionQueueRepository repository;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Routing attributes pool.
     */
    private final RoutingAttributesPool routingAttributesPool;

    /**
     * Instantiates a new Steps service.
     *
     * @param repository            the repository
     * @param precisionQueuesPool   the precision queues pool
     * @param agentsPool            the agents pool
     * @param routingAttributesPool the routing attributes pool
     */
    @Autowired
    public StepsServiceImpl(PrecisionQueueRepository repository,
                            PrecisionQueuesPool precisionQueuesPool, AgentsPool agentsPool,
                            RoutingAttributesPool routingAttributesPool) {
        this.repository = repository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.agentsPool = agentsPool;
        this.routingAttributesPool = routingAttributesPool;
    }

    @Override
    public PrecisionQueueEntity create(String queueId, StepEntity stepEntity) {
        String notFoundMessage = "Queue not found for id: " + queueId;
        PrecisionQueue precisionQueue = this.precisionQueuesPool.findById(queueId);
        if (precisionQueue == null) {
            throw new NotFoundException(notFoundMessage);
        }
        this.validateAndSetRoutingAttributes(stepEntity);
        Step newStep = new Step(stepEntity);
        newStep.evaluateAssociatedAgents(agentsPool.findAll());

        Optional<PrecisionQueueEntity> existing = this.repository.findById(queueId);
        if (!existing.isPresent()) {
            throw new NotFoundException(notFoundMessage);
        }
        precisionQueue.addStep(newStep);
        PrecisionQueueEntity precisionQueueEntity = existing.get();
        precisionQueueEntity.addStep(stepEntity);
        return this.repository.save(precisionQueueEntity);
    }

    @Override
    public PrecisionQueueEntity update(UUID id, String queueId, StepEntity stepEntity) {
        Optional<PrecisionQueueEntity> existing = this.repository.findById(queueId);
        if (!existing.isPresent()) {
            throw new NotFoundException("Queue not found for id: " + queueId);
        }
        stepEntity.setId(id);
        PrecisionQueueEntity precisionQueueEntity = existing.get();
        if (!precisionQueueEntity.containsStep(stepEntity)) {
            throw new NotFoundException("Step: " + id + " not found in queue: " + queueId);
        }
        this.validateAndSetRoutingAttributes(stepEntity);
        precisionQueueEntity.updateStep(stepEntity);
        this.repository.save(precisionQueueEntity);
        // Till now no concurrency issues !!

        Step step = new Step(stepEntity);
        step.evaluateAssociatedAgents(agentsPool.findAll());
        this.precisionQueuesPool.findById(queueId).updateStep(step);
        return precisionQueueEntity;
    }

    @Override
    public ResponseEntity<Object> delete(String queueId, UUID id) {
        Optional<PrecisionQueueEntity> existing = this.repository.findById(queueId);
        if (!existing.isPresent()) {
            throw new NotFoundException("Queue not found for id: " + queueId);
        }
        PrecisionQueueEntity precisionQueueEntity = existing.get();
        boolean deletedFromEntity = precisionQueueEntity.deleteStepById(id);
        if (!deletedFromEntity) {
            throw new NotFoundException("Step: " + id + " not found in queue: " + queueId);
        }
        PrecisionQueue precisionQueue = this.precisionQueuesPool.findById(queueId);
        precisionQueue.deleteStepById(id);
        this.repository.save(precisionQueueEntity);
        return new ResponseEntity<>(new SuccessResponseBody("Successfully Deleted"), HttpStatus.OK);
    }

    /**
     * Validate and set routing attributes.
     *
     * @param stepEntity the step entity
     */
    private void validateAndSetRoutingAttributes(StepEntity stepEntity) {
        List<ExpressionEntity> expressionEntities = stepEntity.getExpressions();
        if (expressionEntities == null) {
            stepEntity.setExpressions(new ArrayList<>());
            return;
        }
        for (ExpressionEntity expressionEntity : expressionEntities) {
            List<TermEntity> termEntities = expressionEntity.getTerms();
            if (termEntities == null) {
                expressionEntity.setTerms(new ArrayList<>());
                continue;
            }
            for (TermEntity termEntity : termEntities) {
                RoutingAttribute requestRoutingAttribute = termEntity.getRoutingAttribute();
                if (requestRoutingAttribute == null) {
                    continue;
                }
                RoutingAttribute routingAttribute = routingAttributesPool.findById(requestRoutingAttribute.getId());
                if (routingAttribute == null) {
                    throw new NotFoundException("Could not find a routing-attribute resource");
                }
                termEntity.setRoutingAttribute(routingAttribute);
            }
        }
    }
}
