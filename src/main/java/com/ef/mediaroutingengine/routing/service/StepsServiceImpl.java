package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ExpressionEntity;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.StepEntity;
import com.ef.cim.objectmodel.TermEntity;
import com.ef.cim.objectmodel.task.Task;
import com.ef.mediaroutingengine.global.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.StepTimerService;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.model.QueueTask;
import com.ef.mediaroutingengine.routing.model.Step;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.pool.RoutingAttributesPool;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.taskmanager.model.TaskStep;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
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
     * The Step timer service.
     */
    private final StepTimerService stepTimerService;
    private final TasksRepository tasksRepository;

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
                            RoutingAttributesPool routingAttributesPool,
                            StepTimerService stepTimerService, TasksRepository tasksRepository) {
        this.repository = repository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.agentsPool = agentsPool;
        this.routingAttributesPool = routingAttributesPool;
        this.stepTimerService = stepTimerService;
        this.tasksRepository = tasksRepository;
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
    public PrecisionQueueEntity update(String id, String queueId, StepEntity stepEntity) {
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
    public ResponseEntity<Object> delete(String queueId, String id) {
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

    @Override
    public Set<CCUser> previewAgentsMatchingStepCriteriaInQueue(String queueId, Optional<String> stepId) {
        logger.info("Retrieving agents matching Steps in Queue : {} .Request initiated.", queueId);
        PrecisionQueue precisionQueue = this.precisionQueuesPool.findById(queueId);
        if (precisionQueue == null) {
            String errorMessage = QUEUE_NOT_FOUND_MSG + queueId;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        Set<CCUser> listOfAllAgentsInStepCriteria = stepId.isPresent()
                ? this.previewAgentsInQueueStepLevel(precisionQueue, stepId)
                : this.previewAgentsInQueueLevel(precisionQueue);

        logger.info("({}) agents retrieved successfully.", listOfAllAgentsInStepCriteria.size());
        return listOfAllAgentsInStepCriteria;
    }

    /**
     * This function returns a list of available agents that exists in precisionQueue X for a specified Step.
     *
     * @param precisionQueue precisionQueue
     * @param stepId         List of agents associated with Step X.
     * @return List of agents in Queue X.
     */
    private Set<CCUser> previewAgentsInQueueStepLevel(@NotNull PrecisionQueue precisionQueue,
                                                            Optional<String> stepId) {
        return precisionQueue.getSteps().stream()
                .filter(step -> step.getId().equals(stepId.get()))
                .flatMap(step -> step.getAssociatedAgents().stream())
                .map(agent -> agent.toCcUser())
                .collect(Collectors.toSet());
    }

    /**
     * This function returns a list of available agents that exists in precisionQueue X.
     *
     * @param precisionQueue precisionQueue.
     * @return List of agents in Queue X.
     */
    private Set<CCUser> previewAgentsInQueueLevel(@NotNull PrecisionQueue precisionQueue) {
        return precisionQueue.getSteps().stream()
                .flatMap(step -> step.getAssociatedAgents().stream())
                .map(agent -> agent.toCcUser())
                .collect(Collectors.toSet());
    }

    private void deleteStep(PrecisionQueue precisionQueue, PrecisionQueueEntity precisionQueueEntity, String id) {
        precisionQueueEntity.deleteStepById(id);
        precisionQueue.deleteStepById(id);
        this.repository.save(precisionQueueEntity);
    }

    private ResponseEntity<Object> onlyOneStep(String queueId, PrecisionQueue precisionQueue,
                                               PrecisionQueueEntity precisionQueueEntity, String id) {
        List<Task> tasks = this.tasksRepository.findAllByQueueId(queueId);
        if (tasks.isEmpty()) {
            deleteStep(precisionQueue, precisionQueueEntity, id);
            logger.info("Step {} deleted from Queue {} successfully", id, queueId);
            return new ResponseEntity<>(new SuccessResponseBody("Successfully Deleted"), HttpStatus.OK);
        } else {
            logger.info("Could not Delete Step {} from Queue {}, there are tasks associated to Queue with "
                    + "only one step configured", id, queueId);
            return new ResponseEntity<>(tasks, HttpStatus.CONFLICT);
        }
    }

    private ResponseEntity<Object> moreThanOneSteps(PrecisionQueue precisionQueue,
                                                    PrecisionQueueEntity precisionQueueEntity,
                                                    String id, int stepIndex) {
        if (stepIndex == precisionQueue.getSteps().size() - 1) {
            lastStep(precisionQueue, stepIndex, id);
        } else {
            notLastStep(precisionQueue, stepIndex, id);
        }
        deleteStep(precisionQueue, precisionQueueEntity, id);
        logger.info("Step {} deleted from Queue {} successfully", id, precisionQueue.getId());
        return new ResponseEntity<>(new SuccessResponseBody("Successfully Deleted"), HttpStatus.OK);
    }

    private void lastStep(PrecisionQueue precisionQueue, int stepIndex, String id) {
        synchronized (precisionQueue.getServiceQueue()) {
            for (QueueTask task : precisionQueue.getTasks()) {
                if (task.getCurrentStep() != null && task.getCurrentStep().getStep().getId().equals(id)) {
                    Step prevStep = precisionQueue.getStepAt(stepIndex - 1);
                    task.setCurrentStep(new TaskStep(prevStep, true));
                }
            }
        }
    }

    private void notLastStep(PrecisionQueue precisionQueue, int stepIndex, String id) {
        synchronized (precisionQueue.getServiceQueue()) {
            for (QueueTask queueTask : precisionQueue.getTasks()) {
                if (queueTask.getCurrentStep() != null && queueTask.getCurrentStep().getStep().getId().equals(id)) {
                    this.stepTimerService.stop(queueTask.getTaskId());
                    this.stepTimerService.startNext(queueTask, precisionQueue, stepIndex + 1);
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
