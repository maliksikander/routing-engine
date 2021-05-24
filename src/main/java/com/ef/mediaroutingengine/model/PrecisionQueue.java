package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.services.TaskScheduler;
import com.ef.mediaroutingengine.services.queue.PriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrecisionQueue implements IQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrecisionQueue.class);
    private UUID id;
    private String name;
    private MediaRoutingDomain mrd;
    private AgentSelectionCriteria agentSelectionCriteria;
    private int serviceLevelType;
    private int serviceLevelThreshold;
    private List<Step> steps;

    private PriorityQueue serviceQueue;
    private TaskScheduler taskScheduler;
    private int[] timeouts;
    private Long averageTalkTime = 0L;
    private Long noOfTask = 0L;
    boolean enableReporting = false;

    public PrecisionQueue() {

    }

    /**
     * Parametrized constructor. Constructs a PrecisionQueue object with a PrecisionQueueEntity object.
     *
     * @param pqEntity the precision-queue entity object Stored in the DB.
     */
    public PrecisionQueue(PrecisionQueueEntity pqEntity) {
        this.id = pqEntity.getId();
        this.name = pqEntity.getName();
        this.mrd = pqEntity.getMrd();
        this.agentSelectionCriteria = pqEntity.getAgentSelectionCriteria();
        this.serviceLevelType = pqEntity.getServiceLevelType();
        this.serviceLevelThreshold = pqEntity.getServiceLevelThreshold();
        this.steps = toSteps(pqEntity.getSteps());
    }

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

    public UUID getId() {
        return id;
    }

    /**
     * Sets the id only if it is null.
     *
     * @param id unique id to set
     */
    public void setId(UUID id) {
        if (this.id == null) {
            this.id = id;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MediaRoutingDomain getMrd() {
        return mrd;
    }

    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    public AgentSelectionCriteria getAgentSelectionCriteria() {
        return agentSelectionCriteria;
    }

    public void setAgentSelectionCriteria(AgentSelectionCriteria agentSelectionCriteria) {
        this.agentSelectionCriteria = agentSelectionCriteria;
    }

    public int getServiceLevelType() {
        return serviceLevelType;
    }

    public void setServiceLevelType(int serviceLevelType) {
        this.serviceLevelType = serviceLevelType;
    }

    public int getServiceLevelThreshold() {
        return serviceLevelThreshold;
    }

    public void setServiceLevelThreshold(int serviceLevelThreshold) {
        this.serviceLevelThreshold = serviceLevelThreshold;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public Step getStepAt(int index) {
        return this.steps.get(index);
    }

    public PriorityQueue getServiceQueue() {
        return serviceQueue;
    }

    public void setServiceQueue(PriorityQueue serviceQueue) {
        this.serviceQueue = serviceQueue;
    }

    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public int[] getTimeouts() {
        return timeouts;
    }

    public void setTimeouts(int[] timeouts) {
        this.timeouts = timeouts;
    }

    public Long getAverageTalkTime() {
        return averageTalkTime;
    }

    public void setAverageTalkTime(Long averageTalkTime) {
        this.averageTalkTime = averageTalkTime;
    }

    public Long getNoOfTask() {
        return noOfTask;
    }

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

    public boolean isEnableReporting() {
        return enableReporting;
    }

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
        LOGGER.debug("Removed Task: {}", task != null ? task.getId() : "task not found" + " from queue: "
                + this.getName());
        printQueue();
        return task;
    }

    @Override
    public void printQueue() {
        LOGGER.debug("FIFO Precision Queue: {}", this.serviceQueue);
    }

    @Override
    public void logAllSteps() {
        int i = 0;
        for (Step step : this.steps) {
            LOGGER.info("Step {}: {}, No. of associated agents: {}", ++i, step, step.getAssociatedAgents().size());
        }
    }

    /**
     * Returns the task at the queue's head without dequeue-ing it.
     *
     * @return the task at the queue's head.
     */
    public Task peek() {
        Task task = null;
        task = this.serviceQueue.dequeue(false); // serviceQueue.peek
        LOGGER.debug("peek task: {}", task != null ? task.getId() : "Task not found");
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
     * Removes the task from the queue by the task id.
     *
     * @param taskId the id task to be removed
     * @return true if found and removed, false otherwise
     */
    public boolean endTask(String taskId) {
//        log.debug("Going to remove task from precision queue");
//        TaskService task = TaskServiceManager.getInstance().getTask(taskId);
//        boolean result = this.removeTask(task);
//        log.debug(result ? "Task abandoned from precision queue" : "Task not found in precision queue");
//        return result;
        return true;
    }

    public boolean isEmpty() {
        return this.serviceQueue.size() < 1;
    }
}
