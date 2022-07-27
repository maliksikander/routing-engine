package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.ExpressionEntity;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.StepEntity;
import com.ef.cim.objectmodel.TermEntity;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.model.Step;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.pool.RoutingAttributesPool;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.model.TaskStep;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * The type Steps service.
 */
@Service
public class StepsServiceImpl implements StepsService {
    private static final String QUEUE_NOT_FOUND_MSG = "Queue not found for id: ";
    private final Logger logger = LoggerFactory.getLogger(StepsServiceImpl.class);
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
        logger.info("Create Step in Queue: {} request initiated", queueId);

        PrecisionQueue precisionQueue = this.precisionQueuesPool.findById(queueId);
        if (precisionQueue == null) {
            String errorMessage = QUEUE_NOT_FOUND_MSG + queueId;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        this.validateAndSetRoutingAttributes(stepEntity);
        logger.debug("Routing Attributes in Step validated successfully");

        Step newStep = new Step(stepEntity);
        logger.debug("New Step created with id: {}", newStep.getId());

        newStep.evaluateAssociatedAgents(agentsPool.findAll());
        logger.debug("Associated agents evaluated in Step: {}", newStep.getId());

        Optional<PrecisionQueueEntity> existing = this.repository.findById(queueId);
        if (existing.isEmpty()) {
            throw new NotFoundException(QUEUE_NOT_FOUND_MSG + queueId);
        }

        precisionQueue.addStep(newStep);
        logger.debug("Step {} added in in-memory PrecisionQueue {}", newStep.getId(), queueId);

        PrecisionQueueEntity precisionQueueEntity = existing.get();
        precisionQueueEntity.addStep(stepEntity);
        PrecisionQueueEntity queueInConfigDb = this.repository.save(precisionQueueEntity);
        logger.debug("Step {} added in PrecisionQueue {} in Config DB", newStep.getId(), queueId);

        logger.info("Step {} added in queue {} successfully", newStep.getId(), queueId);
        return queueInConfigDb;
    }

    @Override
    public PrecisionQueueEntity update(UUID id, String queueId, StepEntity stepEntity) {
        logger.info("Update Step {} in queue {} request initiated", id, queueId);

        Optional<PrecisionQueueEntity> existing = this.repository.findById(queueId);
        if (existing.isEmpty()) {
            String errorMessage = QUEUE_NOT_FOUND_MSG + queueId;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        stepEntity.setId(id);
        PrecisionQueueEntity precisionQueueEntity = existing.get();
        if (!precisionQueueEntity.containsStep(stepEntity)) {
            String errorMessage = "Step: " + id + " not found in queue: " + queueId;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        this.validateAndSetRoutingAttributes(stepEntity);
        logger.debug("Routing Attributes in Step {} validated successfully", id);

        precisionQueueEntity.updateStep(stepEntity);
        this.repository.save(precisionQueueEntity);
        logger.debug("Step {} entity updated in Queue {} in Config DB", id, queueId);

        Step step = new Step(stepEntity);
        step.evaluateAssociatedAgents(agentsPool.findAll());
        logger.debug("Associated agent in Step {} re-evaluated", id);

        this.precisionQueuesPool.findById(queueId).updateStep(step);
        logger.debug("Step {} updated in in-memory Queue {}", id, queueId);

        logger.info("Step {} updated in Queue {} successfully", id, queueId);
        return precisionQueueEntity;
    }

    @Override
    public ResponseEntity<Object> delete(String queueId, UUID id) {
        logger.info("Delete Step {} in Queue {} request initiated", id, queueId);

        PrecisionQueue precisionQueue = this.precisionQueuesPool.findById(queueId);
        if (precisionQueue == null) {
            String errorMessage = QUEUE_NOT_FOUND_MSG + queueId;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        Optional<PrecisionQueueEntity> existing = this.repository.findById(queueId);
        if (existing.isEmpty()) {
            throw new NotFoundException(QUEUE_NOT_FOUND_MSG + queueId);
        }

        int stepIndex = precisionQueue.findStepIndex(id);
        if (stepIndex == -1) {
            String errorMessage = "Step: " + id + " not found in queue: " + queueId;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
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
            logger.info("Step {} deleted from Queue {} successfully", id, queueId);
            return new ResponseEntity<>(new SuccessResponseBody("Successfully Deleted"), HttpStatus.OK);
        } else {
            logger.info("Could not Delete Step {} from Queue {}, there are tasks associated to Queue with "
                    + "only one step configured", id, queueId);
            List<TaskDto> taskDtoList = new ArrayList<>();
            tasks.forEach(task -> taskDtoList.add(AdapterUtility.createTaskDtoFrom(task)));
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
        logger.info("Step {} deleted from Queue {} successfully", id, precisionQueue.getId());
        return new ResponseEntity<>(new SuccessResponseBody("Successfully Deleted"), HttpStatus.OK);
    }

    private void lastStep(PrecisionQueue precisionQueue, int stepIndex, UUID id) {
        synchronized (precisionQueue.getServiceQueue()) {
            for (Task task : precisionQueue.getTasks()) {
                if (task.getCurrentStep() != null && task.getCurrentStep().getStep().getId().equals(id)) {
                    Step prevStep = precisionQueue.getStepAt(stepIndex - 1);
                    task.setCurrentStep(new TaskStep(prevStep, true));
                }
            }
        }
    }

    private void notLastStep(PrecisionQueue precisionQueue, int stepIndex, UUID id) {
        synchronized (precisionQueue.getServiceQueue()) {
            for (Task task : precisionQueue.getTasks()) {
                if (task.getCurrentStep() != null && task.getCurrentStep().getStep().getId().equals(id)) {
                    task.getTimer().cancel();
                    task.setUpStepFrom(precisionQueue, stepIndex + 1);
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
                    String errorMessage = "Failed to validate RoutingAttributes for Step";
                    logger.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                }
                termEntity.setRoutingAttribute(routingAttribute);
            }
        }
    }
}
