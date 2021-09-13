package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.ExpressionEntity;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Step;
import com.ef.mediaroutingengine.model.StepEntity;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TermEntity;
import com.ef.mediaroutingengine.repositories.PrecisionQueueRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.RoutingAttributesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
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
     * The Tasks pool.
     */
    private final TasksPool tasksPool;

    private static final String QUEUE_NOT_FOUND_MSG = "Queue not found for id: ";

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
                            RoutingAttributesPool routingAttributesPool, TasksPool tasksPool) {
        this.repository = repository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.agentsPool = agentsPool;
        this.routingAttributesPool = routingAttributesPool;
        this.tasksPool = tasksPool;
    }

    @Override
    public PrecisionQueueEntity create(String queueId, StepEntity stepEntity) {
        PrecisionQueue precisionQueue = this.precisionQueuesPool.findById(queueId);
        if (precisionQueue == null) {
            throw new NotFoundException(QUEUE_NOT_FOUND_MSG + queueId);
        }
        this.validateAndSetRoutingAttributes(stepEntity);
        Step newStep = new Step(stepEntity);
        newStep.evaluateAssociatedAgents(agentsPool.findAll());

        Optional<PrecisionQueueEntity> existing = this.repository.findById(queueId);
        if (existing.isEmpty()) {
            throw new NotFoundException(QUEUE_NOT_FOUND_MSG + queueId);
        }
        precisionQueue.addStep(newStep);
        PrecisionQueueEntity precisionQueueEntity = existing.get();
        precisionQueueEntity.addStep(stepEntity);
        return this.repository.save(precisionQueueEntity);
    }

    @Override
    public PrecisionQueueEntity update(UUID id, String queueId, StepEntity stepEntity) {
        Optional<PrecisionQueueEntity> existing = this.repository.findById(queueId);
        if (existing.isEmpty()) {
            throw new NotFoundException(QUEUE_NOT_FOUND_MSG + queueId);
        }
        stepEntity.setId(id);
        PrecisionQueueEntity precisionQueueEntity = existing.get();
        if (!precisionQueueEntity.containsStep(stepEntity)) {
            throw new NotFoundException("Step: " + id + " not found in queue: " + queueId);
        }
        this.validateAndSetRoutingAttributes(stepEntity);
        precisionQueueEntity.updateStep(stepEntity);
        this.repository.save(precisionQueueEntity);

        Step step = new Step(stepEntity);
        step.evaluateAssociatedAgents(agentsPool.findAll());
        this.precisionQueuesPool.findById(queueId).updateStep(step);
        return precisionQueueEntity;
    }

    @Override
    public ResponseEntity<Object> delete(String queueId, UUID id) {
        PrecisionQueue precisionQueue = this.precisionQueuesPool.findById(queueId);
        if (precisionQueue == null) {
            throw new NotFoundException(QUEUE_NOT_FOUND_MSG + queueId);
        }
        Optional<PrecisionQueueEntity> existing = this.repository.findById(queueId);
        if (existing.isEmpty()) {
            throw new NotFoundException(QUEUE_NOT_FOUND_MSG + queueId);
        }
        int stepIndex = precisionQueue.findStepIndex(id);
        if (stepIndex == -1) {
            throw new NotFoundException("Step: " + id + " not found in queue: " + queueId);
        }

        int noOfSteps = precisionQueue.getSteps().size();

        if (noOfSteps == 1) {
            return onlyOneStep(queueId, precisionQueue, existing.get(), id);
        } else {
            return moreThanOneSteps(precisionQueue, existing.get(), id, stepIndex);
        }
    }

    private void deleteStep(PrecisionQueue precisionQueue, PrecisionQueueEntity precisionQueueEntity, UUID id) {
        precisionQueueEntity.deleteStepById(id);
        precisionQueue.deleteStepById(id);
        this.repository.save(precisionQueueEntity);
    }

    private ResponseEntity<Object> onlyOneStep(String queueId, PrecisionQueue precisionQueue,
                                               PrecisionQueueEntity precisionQueueEntity, UUID id) {
        List<Task> tasks = this.tasksPool.findByQueueId(queueId);
        if (tasks.isEmpty()) {
            deleteStep(precisionQueue, precisionQueueEntity, id);
            return new ResponseEntity<>(new SuccessResponseBody("Successfully Deleted"), HttpStatus.OK);
        } else {
            List<TaskDto> taskDtoList = new ArrayList<>();
            tasks.forEach(task -> taskDtoList.add(new TaskDto(task)));
            return new ResponseEntity<>(taskDtoList, HttpStatus.CONFLICT);
        }
    }

    private ResponseEntity<Object> moreThanOneSteps(PrecisionQueue precisionQueue,
                                                    PrecisionQueueEntity precisionQueueEntity,
                                                    UUID id, int stepIndex) {
        if (stepIndex == precisionQueue.getSteps().size() - 1) {
            lastStep(precisionQueue, stepIndex, id);
        } else {
            notLastStep(precisionQueue, stepIndex, id);
        }
        deleteStep(precisionQueue, precisionQueueEntity, id);
        return new ResponseEntity<>(new SuccessResponseBody("Successfully Deleted"), HttpStatus.OK);
    }

    private void lastStep(PrecisionQueue precisionQueue, int stepIndex, UUID id) {
        for (Task task : precisionQueue.getTasks()) {
            if (task.getCurrentStep() != null && task.getCurrentStep().getId().equals(id)) {
                Step prevStep = precisionQueue.getStepAt(stepIndex - 1);
                task.setCurrentStep(prevStep);
            }
        }
    }

    private void notLastStep(PrecisionQueue precisionQueue, int stepIndex, UUID id) {
        for (Task task : precisionQueue.getTasks()) {
            if (task.getCurrentStep() != null && task.getCurrentStep().getId().equals(id)) {
                task.getTimer().cancel();
                int nextStepIndex = stepIndex + 1;
                task.setCurrentStep(precisionQueue.getStepAt(nextStepIndex));
                if (nextStepIndex < precisionQueue.getSteps().size() - 1) {
                    task.startTimer();
                }
            }
        }
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
