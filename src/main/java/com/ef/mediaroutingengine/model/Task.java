package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskDto;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Task.
 */
public class Task {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(Task.class);
    /**
     * The ID.
     */
    private final UUID id;
    /**
     * The Channel session.
     */
    private final ChannelSession channelSession;
    /**
     * The Mrd.
     */
    private final MediaRoutingDomain mrd;
    /**
     * The Queue.
     */
    private final String queue;
    /**
     * The Timer.
     */
    private Timer timer;
    /**
     * The Change support.
     */
    private final PropertyChangeSupport changeSupport;
    /**
     * The Priority.
     */
    private int priority;
    /**
     * The State.
     */
    private TaskState state;
    /**
     * The Assigned to.
     */
    private UUID assignedTo;
    /**
     * The Enqueue time.
     */
    private Long enqueueTime;
    /**
     * The Start time.
     */
    private Long startTime;
    /**
     * The Handling time.
     */
    private Long handlingTime;
    /**
     * The Agent request timeout.
     */
    private boolean agentRequestTimeout;
    /**
     * The Current step.
     */
    private TaskStep currentStep;

    private Task(UUID id, ChannelSession channelSession, MediaRoutingDomain mrd, String queue) {
        this.id = id;
        this.channelSession = channelSession;
        this.mrd = mrd;
        this.queue = queue;

        this.priority = 1; // Right now hardcoded at highest priority level
        this.enqueueTime = System.currentTimeMillis();
        this.timer = new Timer();
        this.handlingTime = 0L;
        this.agentRequestTimeout = false;
        this.changeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Gets instance.
     *
     * @param channelSession the channel session
     * @param mrd            the mrd
     * @param queue          the queue
     * @param state          the state
     * @return the instance
     */
    public static Task getInstance(ChannelSession channelSession, MediaRoutingDomain mrd,
                                   String queue, TaskState state) {
        Task task = new Task(UUID.randomUUID(), channelSession, mrd, queue);
        task.setTaskState(state);
        return task;
    }

    /**
     * Gets instance.
     *
     * @param taskDto the task dto
     * @return the instance
     */
    public static Task getInstance(TaskDto taskDto) {
        Task task = new Task(taskDto.getId(), taskDto.getChannelSession(), taskDto.getMrd(), taskDto.getQueue());
        task.state = taskDto.getState();
        task.priority = taskDto.getPriority();
        task.assignedTo = taskDto.getAssignedTo();
        task.enqueueTime = taskDto.getEnqueueTime();
        return task;
    }

    /**
     * Gets instance.
     *
     * @param oldTask the old task
     * @return the instance
     */
    public static Task getInstance(Task oldTask) {
        TaskState newTaskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        Task task = getInstance(oldTask.channelSession, oldTask.mrd, oldTask.queue, newTaskState);
        task.priority = 11;
        return task;
    }

    /**
     * Gets instance.
     *
     * @param agentId        the agent id
     * @param mrd            the mrd
     * @param channelSession the channel session
     * @return the instance
     */
    public static Task getInstance(UUID agentId, MediaRoutingDomain mrd, ChannelSession channelSession) {
        TaskState taskState = new TaskState(Enums.TaskStateName.ACTIVE, null);
        Task task = getInstance(channelSession, mrd, null, taskState);
        task.setAssignedTo(agentId);
        return task;
    }

    // +++++++++++++++++++++++++++++++ Accessor Methods ++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Gets id.
     *
     * @return the id
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Gets channel session.
     *
     * @return the channel session
     */
    public ChannelSession getChannelSession() {
        return channelSession;
    }

    /**
     * Gets mrd.
     *
     * @return the mrd
     */
    public MediaRoutingDomain getMrd() {
        return this.mrd;
    }

    /**
     * Gets queue.
     *
     * @return the queue
     */
    public String getQueue() {
        return this.queue;
    }

    /**
     * Gets priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * Sets priority.
     *
     * @param priority the priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Gets task state.
     *
     * @return the task state
     */
    public TaskState getTaskState() {
        return this.state;
    }

    /**
     * Sets task state.
     *
     * @param state the state
     */
    public void setTaskState(TaskState state) {
        this.state = state;
    }

    /**
     * Gets assigned to.
     *
     * @return the assigned to
     */
    public UUID getAssignedTo() {
        return assignedTo;
    }

    /**
     * Sets assigned to.
     *
     * @param assignedTo the assigned to
     */
    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * Gets enqueue time.
     *
     * @return the enqueue time
     */
    public Long getEnqueueTime() {
        return enqueueTime;
    }

    /**
     * Sets enqueue time.
     *
     * @param enqueueTime the enqueue time
     */
    public void setEnqueueTime(Long enqueueTime) {
        this.enqueueTime = enqueueTime;
    }

    /**
     * Gets start time.
     *
     * @return the start time
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Sets start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets handling time.
     *
     * @return the handling time
     */
    public Long getHandlingTime() {
        return handlingTime;
    }

    /**
     * Sets handling time.
     *
     * @param handlingTime the handling time
     */
    public void setHandlingTime(Long handlingTime) {
        this.handlingTime = handlingTime + this.handlingTime;
    }

    /**
     * Gets timer.
     *
     * @return the timer
     */
    public Timer getTimer() {
        return this.timer;
    }

    public TaskStep getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(TaskStep currentStep) {
        this.currentStep = currentStep;
    }

    public void setUpStepFrom(PrecisionQueue precisionQueue, int stepStartingIndex) {
        this.currentStep = precisionQueue.getNextStep(stepStartingIndex);
        this.startStepTimer();
    }

    /**
     * Gets topic id.
     *
     * @return the topic id
     */
    public UUID getTopicId() {
        return this.channelSession.getTopicId();
    }

    public RoutingMode getRoutingMode() {
        return this.channelSession.getChannel().getChannelConfig().getRoutingPolicy().getRoutingMode();
    }

    /**
     * Gets last assigned agent id.
     *
     * @return the last assigned agent id
     */
    // TODO: Implement it correctly
    public UUID getLastAssignedAgentId() {
        return UUID.randomUUID();
    }

    /**
     * Add property change listener.
     *
     * @param property the property
     * @param listener the listener
     */
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(property, listener);
    }

    /**
     * Remove property change listener.
     *
     * @param property the property
     * @param listener the listener
     */
    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(property, listener);
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Start timer.
     */
    private void startStepTimer() {
        try {
            if (this.currentStep != null && !this.currentStep.isLastStep()) {
                timer = new Timer();
                timer.schedule(new TaskTimer(), this.currentStep.getStep().getTimeout() * 1000L);
                logger.debug("Step: {} timer started for task: {}", currentStep.getStep().getId(), this.id);
            }
        } catch (IllegalArgumentException ex) {
            if (!"Negative delay.".equalsIgnoreCase(ExceptionUtils.getRootCause(ex).getMessage())) {
                logger.error(ExceptionUtils.getMessage(ex));
                logger.error(ExceptionUtils.getStackTrace(ex));
            }
        } catch (Exception ex) {
            logger.error(ExceptionUtils.getMessage(ex));
            logger.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * Handle task remove event.
     */
    public void handleTaskRemoveEvent() {
        this.changeSupport.firePropertyChange(Enums.EventName.TASK_REMOVED.name(), null, "");
    }

    /**
     * Agent request timeout.
     */
    public void agentRequestTimeout() {
        this.agentRequestTimeout = true;
    }

    /**
     * Is agent request timeout boolean.
     *
     * @return the boolean
     */
    public boolean isAgentRequestTimeout() {
        return this.agentRequestTimeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task that = (Task) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TaskId: " + this.id + ", MRD: " + this.mrd + ", Priority: " + this.getPriority();
    }

    /**
     * The type Task timer.
     */
// +++++++++++++++++++++++++++++++ TaskTimer class ++++++++++++++++++++++++++++++++++++++++++++
    private class TaskTimer extends TimerTask {

        public void run() {
            logger.debug("Time up for step: {}, Task id: {}, MRD: {}", Task.this.currentStep.getStep().getId(),
                    Task.this.getId(), Task.this.getMrd());
            try {
                Task.this.getTimer().cancel();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Task.this.changeSupport.firePropertyChange(Enums.EventName.TIMER.name(), null, Task.this);
        }
    }
    // +++++++++++++++++++++++++++++++************************++++++++++++++++++++++++++++++++++++++++++++*
}
