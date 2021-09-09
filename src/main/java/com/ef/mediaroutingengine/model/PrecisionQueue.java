package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.services.TaskRouter;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.queue.PriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Precision queue.
 */
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
     * The Service queue.
     */
    private final PriorityQueue serviceQueue;
    /**
     * The Task scheduler.
     */
    private final TaskRouter taskRouter;
    /**
     * Enable reporting.
     */
    boolean enableReporting;
    /**
     * The Name.
     */
    private String name;
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
     * The Steps.
     */
    private final List<Step> steps;
    /**
     * The Average talk time.
     */
    private Long averageTalkTime;
    /**
     * The No of task.
     */
    private Long noOfTask;

    /**
     * Parametrized constructor. Constructs a PrecisionQueue object with a PrecisionQueueEntity object.
     *
     * @param pqEntity      the precision-queue entity object Stored in the DB.
     * @param agentsPool    the agents pool
     * @param taskRouter the task scheduler
     */
    public PrecisionQueue(PrecisionQueueEntity pqEntity, AgentsPool agentsPool, TaskRouter taskRouter) {
        this.id = pqEntity.getId();
        this.name = pqEntity.getName();
        this.mrd = pqEntity.getMrd();
        this.serviceLevelType = pqEntity.getServiceLevelType();
        this.serviceLevelThreshold = pqEntity.getServiceLevelThreshold();
        this.steps = toSteps(pqEntity.getSteps());
        this.evaluateAgentsAssociatedWithSteps(agentsPool.findAll());

        this.serviceQueue = new PriorityQueue();
        this.taskRouter = taskRouter;
        this.taskRouter.init(this.name, this);

        this.averageTalkTime = 0L;
        this.noOfTask = 0L;
    }

    /**
     * Instantiates a new Precision queue.
     *
     * @param entity        the request body
     * @param taskRouter the task scheduler
     */
    public PrecisionQueue(PrecisionQueueEntity entity, TaskRouter taskRouter) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.mrd = entity.getMrd();
        this.serviceLevelType = entity.getServiceLevelType();
        this.serviceLevelThreshold = entity.getServiceLevelThreshold();
        this.steps = new ArrayList<>();

        this.serviceQueue = new PriorityQueue();
        this.taskRouter = taskRouter;
        this.taskRouter.init(this.name, this);

        this.averageTalkTime = 0L;
        this.noOfTask = 0L;
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
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
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
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets mrd.
     *
     * @return the mrd
     */
    public MediaRoutingDomain getMrd() {
        return mrd;
    }

    /**
     * Sets mrd.
     *
     * @param mrd the mrd
     */
    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    /**
     * Gets service level type.
     *
     * @return the service level type
     */
    public int getServiceLevelType() {
        return serviceLevelType;
    }

    /**
     * Sets service level type.
     *
     * @param serviceLevelType the service level type
     */
    public void setServiceLevelType(int serviceLevelType) {
        this.serviceLevelType = serviceLevelType;
    }

    /**
     * Gets service level threshold.
     *
     * @return the service level threshold
     */
    public int getServiceLevelThreshold() {
        return serviceLevelThreshold;
    }

    /**
     * Sets service level threshold.
     *
     * @param serviceLevelThreshold the service level threshold
     */
    public void setServiceLevelThreshold(int serviceLevelThreshold) {
        this.serviceLevelThreshold = serviceLevelThreshold;
    }

    /**
     * Gets steps.
     *
     * @return the steps
     */
    public List<Step> getSteps() {
        return steps;
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
     * Delete step.
     *
     * @param step the step
     */
    public void deleteStep(Step step) {
        this.steps.remove(step);
    }

    /**
     * Delete step by id.
     *
     * @param id the id
     */
    public void deleteStepById(UUID id) {
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
    private int findStepIndex(UUID id) {
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
    }

    /**
     * Gets service queue.
     *
     * @return the service queue
     */
    public PriorityQueue getServiceQueue() {
        return serviceQueue;
    }

    /**
     * Gets tasks.
     *
     * @return the tasks
     */
    public List<Task> getTasks() {
        return this.serviceQueue.getEnqueuedTasksList();
    }

    /**
     * Gets task scheduler.
     *
     * @return the task scheduler
     */
    public TaskRouter getTaskScheduler() {
        return taskRouter;
    }

    /**
     * Returns the list of timeout of every step in the queue.
     *
     * @return the list of timeout of every step in the queue.
     */
    public List<Integer> getTimeouts() {
        List<Integer> timeouts = new ArrayList<>();
        for (Step step : this.steps) {
            timeouts.add(step.getTimeout());
        }
        if (!timeouts.isEmpty()) {
            timeouts.set(timeouts.size() - 1, -1);
        }
        return timeouts;
    }

    /**
     * Gets average talk time.
     *
     * @return the average talk time
     */
    public Long getAverageTalkTime() {
        return averageTalkTime;
    }

    /**
     * Sets average talk time.
     *
     * @param averageTalkTime the average talk time
     */
    public void setAverageTalkTime(Long averageTalkTime) {
        this.averageTalkTime = averageTalkTime;
    }

    /**
     * Gets no of task.
     *
     * @return the no of task
     */
    public Long getNoOfTask() {
        return noOfTask;
    }

    /**
     * Sets no of task.
     *
     * @param noOfTask the no of task
     */
    public void setNoOfTask(Long noOfTask) {
        this.noOfTask = noOfTask;
    }

    /**
     * Increments the number of tasks.
     */
    public void incrNoOfTask() {
        if (this.noOfTask == null) {
            this.noOfTask = 1L;
        } else {
            this.noOfTask += 1L;
        }
    }

    /**
     * Is enable reporting boolean.
     *
     * @return the boolean
     */
    public boolean isEnableReporting() {
        return enableReporting;
    }

    /**
     * Sets enable reporting.
     *
     * @param enableReporting value of enable reporting
     */
    public void setEnableReporting(boolean enableReporting) {
        this.enableReporting = enableReporting;
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
    public boolean enqueue(Task task) {
        if (task == null || this.serviceQueue.taskExists(task.getId())) {
            return false;
        }
        boolean isEnqueued = serviceQueue.enqueue(task);
        printQueue();
        return isEnqueued;
    }

    @Override
    public Task dequeue() {
        Task task = this.serviceQueue.dequeue(true); //serviceQueue.poll
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
    public Task peek() {
        Task task = this.serviceQueue.dequeue(false); // serviceQueue.peek
        logger.debug("peek task: {}", task != null ? task.getId() : "Task not found");
        return task;
    }

    /**
     * Removes the task from the service-queue.
     *
     * @param task the task to be removed.
     * @return true if found and removed, false otherwise
     */
    public boolean removeTask(Task task) {
        if (task == null) {
            return false;
        }
        return this.serviceQueue.remove(task);
    }

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return this.serviceQueue.size() < 1;
    }
}
