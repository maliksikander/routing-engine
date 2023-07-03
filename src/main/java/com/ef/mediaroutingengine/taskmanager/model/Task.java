package com.ef.mediaroutingengine.taskmanager.model;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.TaskAgent;
import com.ef.cim.objectmodel.TaskQueue;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final String id;
    /**
     * The Mrd.
     */
    private MediaRoutingDomain mrd;
    /**
     * The Queue.
     */
    private final TaskQueue queue;
    /**
     * The Change support.
     */
    private final PropertyChangeSupport changeSupport;
    /**
     * The Mark for deletion.
     */
    private AtomicBoolean markForDeletion = new AtomicBoolean(false);
    /**
     * The Channel session.
     */
    private ChannelSession channelSession;
    /**
     * The Timer.
     */
    private Timer timer;
    /**
     * The Priority.
     */
    private int priority;
    /**
     * The State.
     */
    private TaskState state;
    /**
     * The task type.
     */
    private TaskType type;
    /**
     * The Assigned to.
     */
    private TaskAgent assignedTo;
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
     * The Current step.
     */
    private TaskStep currentStep;

    /**
     * Instantiates a new Task.
     *
     * @param id             the id
     * @param channelSession the channel session
     * @param mrd            the mrd
     * @param queue          the queue
     */
    private Task(String id, ChannelSession channelSession, MediaRoutingDomain mrd, TaskQueue queue, TaskType type) {
        this.id = id;
        this.channelSession = channelSession;
        this.mrd = mrd;
        this.queue = queue;

        this.priority = 1; // Right now hardcoded at highest priority level
        this.enqueueTime = System.currentTimeMillis();
        this.timer = new Timer();
        this.handlingTime = 0L;
        this.changeSupport = new PropertyChangeSupport(this);
        this.type = type;
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
    public static Task getInstanceFrom(ChannelSession channelSession, MediaRoutingDomain mrd,
                                       TaskQueue queue, TaskState state, TaskType type, int priority) {
        Task task = new Task(UUID.randomUUID().toString(), channelSession, mrd, queue, type);
        task.setTaskState(state);
        task.setPriority(priority);
        if (Enums.TaskTypeDirection.DIRECT_TRANSFER.equals(task.getType().getDirection())
                || Enums.TaskTypeDirection.DIRECT_CONFERENCE.equals(task.getType().getDirection())) {
            task.setPriority(11);
        }
        if (state.getName().equals(Enums.TaskStateName.ACTIVE)) {
            task.setStartTime(System.currentTimeMillis());
        }

        return task;
    }

    /**
     * Gets instance.
     *
     * @param taskDto the task dto
     * @return the instance
     */
    public static Task getInstanceFrom(TaskDto taskDto) {
        Task task = new Task(taskDto.getId(), taskDto.getChannelSession(), taskDto.getMrd(),
                taskDto.getQueue(), taskDto.getType());
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
    public static Task getInstanceFrom(Task oldTask) {
        TaskState newTaskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        Task task = getInstanceFrom(oldTask.channelSession, oldTask.mrd, oldTask.queue, newTaskState,
                oldTask.getType(), 11);
       // task.priority = 11;
        return task;
    }

    /**
     * Gets instance.
     *
     * @param agent        the agent
     * @param mrd            the mrd
     * @param channelSession the channel session
     * @return the instance
     */
    public static Task getInstanceFrom(TaskAgent agent, MediaRoutingDomain mrd,
                                       TaskState taskState, ChannelSession channelSession, TaskType type,
                                       int priority) {
        Task task = getInstanceFrom(channelSession, mrd, null, taskState, type, priority);
        task.setAssignedTo(agent);
        return task;
    }

    // +++++++++++++++++++++++++++++++ Accessor Methods ++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
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

    public void setChannelSession(ChannelSession channelSession) {
        this.channelSession = channelSession;
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
    public TaskQueue getQueue() {
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
     * Sets task state.
     *
     * @return the task type
     */
    public TaskType getType() {
        return type;
    }

    /**
     * Sets task state.
     *
     * @param type the type of task
     */
    public void setType(TaskType type) {
        this.type = type;
    }

    /**
     * Gets assigned to.
     *
     * @return the assigned to
     */
    public TaskAgent getAssignedTo() {
        return assignedTo;
    }

    /**
     * Sets assigned to.
     *
     * @param assignedTo the assigned to
     */
    public void setAssignedTo(TaskAgent assignedTo) {
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

    /**
     * Gets current step.
     *
     * @return the current step
     */
    public TaskStep getCurrentStep() {
        return currentStep;
    }

    /**
     * Sets current step.
     *
     * @param currentStep the current step
     */
    public void setCurrentStep(TaskStep currentStep) {
        this.currentStep = currentStep;
    }


    /**
     * Sets the MRD.
     *
     * @param mrd The MRD
     */
    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    /**
     * Sets up step from.
     *
     * @param precisionQueue    the precision queue
     * @param stepStartingIndex the step starting index
     */
    public void setUpStepFrom(PrecisionQueue precisionQueue, int stepStartingIndex) {
        this.currentStep = precisionQueue.getNextStep(stepStartingIndex);
        this.startStepTimer();
    }

    /**
     * Gets topic id.
     *
     * @return the topic id
     */
    public String getTopicId() {
        return this.channelSession.getConversationId();
    }

    /**
     * Gets routing mode.
     *
     * @return the routing mode
     */
    public RoutingMode getRoutingMode() {
        return this.channelSession.getChannel().getChannelConfig().getRoutingPolicy().getRoutingMode();
    }

    /**
     * Gets last assigned agent id.
     *
     * @return the last assigned agent id
     */
// TODO: Implement it correctly
    public String getLastAssignedAgentId() {
        return null;
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
     * Mark for deletion.
     */
    public void markForDeletion() {
        this.markForDeletion.compareAndSet(false, true);
    }

    /**
     * Is marked for deletion boolean.
     *
     * @return the boolean
     */
    public boolean isMarkedForDeletion() {
        return this.markForDeletion.get();
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
            Task.this.changeSupport.firePropertyChange(Enums.EventName.STEP_TIMEOUT.name(), null, Task.this);
        }
    }
    // +++++++++++++++++++++++++++++++************************++++++++++++++++++++++++++++++++++++++++++++*
}
