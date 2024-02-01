package com.ef.mediaroutingengine.routing.model;

import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.StepEntity;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.cim.objectmodel.task.TaskQueue;
import com.ef.mediaroutingengine.routing.TaskRouter;
import com.ef.mediaroutingengine.routing.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.queue.PriorityQueue;
import com.ef.mediaroutingengine.taskmanager.model.TaskStep;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Precision queue.
 */
@Getter
@Setter
public class PrecisionQueue implements Queue {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(PrecisionQueue.class);
    /**
     * The ID.
     */
    private String id;
    /**
     * The Name.
     */
    private String name;
    /**
     * The Service queue.
     */
    private final PriorityQueue serviceQueue;
    /**
     * The Task scheduler.
     */
    private final TaskRouter taskRouter;
    /**
     * The Steps.
     */
    private final List<Step> steps;
    /**
     * The Mrd.
     */
    private MediaRoutingDomain mrd;
    /**
     * The Service level type.
     */
    private int serviceLevelType;
    /**
     * The Service level threshold.
     */
    private int serviceLevelThreshold;
    /**
     * The Agent service level duration.
     */
    private Integer agentSlaDuration;
    /**
     * Minimum value cap for EWT.
     */
    private Integer ewtMinValue;
    /**
     * Maximum value cap for EWT.
     */
    private Integer ewtMaxValue;

    /**
     * Parametrized constructor. Constructs a PrecisionQueue object with a PrecisionQueueEntity object.
     *
     * @param pqEntity   the precision-queue entity object Stored in the DB.
     * @param agentsPool the agents pool
     * @param taskRouter the task scheduler
     */
    public PrecisionQueue(PrecisionQueueEntity pqEntity, AgentsPool agentsPool, TaskRouter taskRouter) {
        this.id = pqEntity.getId();
        this.name = pqEntity.getName();
        this.mrd = pqEntity.getMrd();
        this.serviceLevelType = pqEntity.getServiceLevelType();
        this.serviceLevelThreshold = pqEntity.getServiceLevelThreshold();
        this.agentSlaDuration = pqEntity.getAgentSlaDuration();
        this.ewtMinValue = pqEntity.getEwtMinValue();
        this.ewtMaxValue = pqEntity.getEwtMaxValue();
        this.steps = toSteps(pqEntity.getSteps());
        this.evaluateAgentsAssociatedWithSteps(agentsPool.findAll());

        this.serviceQueue = new PriorityQueue();
        this.taskRouter = taskRouter;
        this.taskRouter.init(this);
    }

    /**
     * Instantiates a new Precision queue.
     *
     * @param entity     the request body
     * @param taskRouter the task scheduler
     */
    public PrecisionQueue(PrecisionQueueEntity entity, TaskRouter taskRouter) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.mrd = entity.getMrd();
        this.serviceLevelType = entity.getServiceLevelType();
        this.serviceLevelThreshold = entity.getServiceLevelThreshold();
        this.agentSlaDuration = entity.getAgentSlaDuration();
        this.steps = new ArrayList<>();

        this.serviceQueue = new PriorityQueue();
        this.taskRouter = taskRouter;
        this.taskRouter.init(this);
    }

    /**
     * To steps list.
     *
     * @param stepEntities the step entities
     * @return the list
     */
    private List<Step> toSteps(List<StepEntity> stepEntities) {
        if (stepEntities == null) {
            return new ArrayList<>();
        }
        List<Step> elements = new ArrayList<>();
        for (StepEntity stepEntity : stepEntities) {
            elements.add(new Step(stepEntity));
        }
        return elements;
    }

    /**
     * Sets the id only if it is null.
     *
     * @param id unique id to set
     */
    public void setId(String id) {
        if (this.id == null) {
            this.id = id;
        }
    }

    /**
     * Gets step at.
     *
     * @param index the index
     * @return the step at
     */
    public Step getStepAt(int index) {
        synchronized (this.steps) {
            return this.steps.get(index);
        }
    }

    /**
     * Gets next step.
     *
     * @param fromIndex search for next task from this index to the end of step list.
     * @return the next step
     */
    public TaskStep getNextStep(int fromIndex) {
        synchronized (this.steps) {
            for (int i = fromIndex; i < this.steps.size(); i++) {
                if (this.steps.get(i).getTimeout() > 0) {
                    boolean isLastStep = i == this.steps.size() - 1;
                    return new TaskStep(this.steps.get(i), isLastStep);
                }
            }
            if (!steps.isEmpty()) {
                return new TaskStep(this.steps.get(this.steps.size() - 1), true);
            }
            return null;
        }
    }

    /**
     * Gets step index.
     *
     * @param step the step
     * @return the step index
     */
    public int getStepIndex(Step step) {
        if (step == null) {
            return -1;
        }
        return this.steps.indexOf(step);
    }

    /**
     * Add step.
     *
     * @param step the step
     */
    public void addStep(Step step) {
        if (this.steps.size() >= 10) {
            throw new IllegalStateException("Only 10 steps are allowed on this queue");
        }
        if (step != null) {
            synchronized (this.steps) {
                this.steps.add(step);
            }
        }
    }

    /**
     * Delete step by id.
     *
     * @param id the id
     */
    public void deleteStepById(String id) {
        int index = findStepIndex(id);
        synchronized (this.steps) {
            if (index > -1) {
                this.steps.remove(index);
            }
        }
    }

    /**
     * Find step index int.
     *
     * @param id the id
     * @return the int
     */
    public int findStepIndex(String id) {
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Update step.
     *
     * @param step the step
     */
    public void updateStep(Step step) {
        synchronized (this.steps) {
            for (int i = 0; i < this.steps.size(); i++) {
                if (this.steps.get(i).equals(step)) {
                    this.steps.set(i, step);
                    break;
                }
            }
        }
    }

    /**
     * Update queue.
     *
     * @param requestBody the request body
     */
    public void updateQueue(PrecisionQueueRequestBody requestBody) {
        this.setName(requestBody.getName());
        this.setMrd(requestBody.getMrd());
        this.setServiceLevelType(requestBody.getServiceLevelType());
        this.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());
        this.setAgentSlaDuration(requestBody.getAgentSlaDuration());
    }

    /**
     * Gets tasks.
     *
     * @return the tasks
     */
    @JsonIgnore
    public List<QueueTask> getTasks() {
        return this.serviceQueue.getAll();
    }

    /**
     * Gets position.
     *
     * @param task the task
     * @return the position
     */
    public int getPosition(Task task) {
        TaskMedia media = task.findMediaByState(TaskMediaState.QUEUED);
        if (media != null) {
            return this.serviceQueue.getPosition(task.getId(), media.getPriority());
        }
        return -1;
    }

    /**
     * Evaluates the agents associated with each step in this precision-queue.
     *
     * @param allAgents List of all agents in the configuration DB
     */
    public void evaluateAgentsAssociatedWithSteps(List<Agent> allAgents) {
        if (steps == null) {
            return;
        }
        for (Step step : steps) {
            step.evaluateAssociatedAgents(allAgents);
        }
    }

    /**
     * Evaluate agent associated with steps.
     *
     * @param agent the agent
     */
    public void evaluateAssociatedAgentOnInsert(Agent agent) {
        if (steps != null) {
            for (Step step : steps) {
                step.evaluateAssociatedAgentOnInsert(agent);
            }
        }
    }

    /**
     * Evaluate associated agent on update.
     *
     * @param agent the agent
     */
    public void evaluateAssociatedAgentOnUpdate(Agent agent) {
        if (steps != null) {
            for (Step step : steps) {
                step.evaluateAssociatedAgentOnUpdate(agent);
            }
        }
    }

    /**
     * Delete associated agent from all.
     *
     * @param agent the agent
     */
    public void deleteAssociatedAgentFromAll(Agent agent) {
        this.steps.forEach(step -> step.removeAssociatedAgent(agent.getId()));
    }

    @Override
    public boolean enqueue(QueueTask task) {
        if (task == null || this.serviceQueue.exists(task.getId())) {
            return false;
        }
        boolean isEnqueued = serviceQueue.enqueue(task);
        printQueue();
        return isEnqueued;
    }

    @Override
    public QueueTask dequeue() {
        QueueTask task = this.serviceQueue.dequeue(true); //serviceQueue.poll
        logger.debug("Removed Task: {}", task != null ? task.getId() : "task not found" + " from queue: "
                + this.getName());
        printQueue();
        return task;
    }

    @Override
    public void printQueue() {
        logger.debug("FIFO Precision Queue: {}", this.serviceQueue);
    }

    @Override
    public void logAllSteps() {
        int i = 0;
        for (Step step : this.steps) {
            logger.info("Step {}: {}, No. of associated agents: {}", ++i, step, step.getAssociatedAgents().size());
        }
    }

    /**
     * Returns the task at the queue's head without dequeue-ing it.
     *
     * @return the task at the queue's head.
     */
    public QueueTask peek() {
        QueueTask task = this.serviceQueue.dequeue(false); // serviceQueue.peek
        logger.debug("peek task: {}", task != null ? task.getId() : "Task not found");
        return task;
    }

    /**
     * Remove by task id boolean.
     *
     * @param taskId the task id
     */
    public void removeTask(String taskId) {
        this.serviceQueue.remove(taskId);
    }

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return this.serviceQueue.size() < 1;
    }

    /**
     * Gets all associated agents.
     *
     * @return the all associated agents
     */
    public List<Agent> getAssociatedAgents() {
        if (this.steps == null || this.steps.isEmpty()) {
            return new ArrayList<>();
        }

        // To retrieve Unique Agents from Steps
        Map<String, Agent> agentMap = new ConcurrentHashMap<>();
        for (Step step : this.steps) {
            for (Agent agent : step.getAssociatedAgents()) {
                agentMap.putIfAbsent(agent.getId(), agent);
            }
        }

        List<Agent> agents = new ArrayList<>();
        agentMap.forEach((k, v) -> agents.add(v));
        return agents;
    }

    public TaskQueue toTaskQueue() {
        return new TaskQueue(this.id, this.name);
    }
}
