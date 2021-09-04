package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskDto;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.List;
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
public class Task implements Serializable {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    /**
     * The Id.
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
    private final Timer timer;
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
     * The Timeouts.
     */
    private List<Integer> timeouts;
    /**
     * The Current step.
     */
    private int currentStep;
    /**
     * The Agent request timeout.
     */
    private boolean agentRequestTimeout;
    private Step step;

    /**
     * Default constructor.
     *
     * @param channelSession the channel session in request
     * @param mrd            associated media routing domain
     * @param queue          the queue
     */
    public Task(ChannelSession channelSession, MediaRoutingDomain mrd, String queue, TaskState state) {
        this.id = UUID.randomUUID();
        this.channelSession = channelSession;
        this.mrd = mrd;
        this.queue = queue;
        //new TaskState(Enums.TaskStateName.QUEUED, null)
        this.priority = 1; // Right now hardcoded at highest priority level
        this.state = state;
        this.enqueueTime = System.currentTimeMillis();
        this.timer = new Timer();
        this.handlingTime = 0L;
        this.agentRequestTimeout = false;

        this.changeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Constructor. build object from TaskDto Object.
     *
     * @param taskDto build object from this dto.
     */
    public Task(TaskDto taskDto) {
        this.id = taskDto.getId();
        this.channelSession = taskDto.getChannelSession();
        this.mrd = taskDto.getMrd();
        this.queue = taskDto.getQueue();

        this.priority = taskDto.getPriority();
        this.state = taskDto.getState();
        this.assignedTo = taskDto.getAssignedTo();
        this.enqueueTime = taskDto.getEnqueueTime();
        this.handlingTime = 0L;

        this.timer = new Timer();
        this.changeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Copy constructor. Used to make a new task for rerouting.
     *
     * @param o the task to be copied
     */
    public Task(Task o) {
        this.id = UUID.randomUUID();
        this.channelSession = o.getChannelSession();
        this.mrd = o.getMrd();
        this.queue = o.getQueue();

        this.priority = 11;
        this.state = new TaskState(Enums.TaskStateName.QUEUED, null);
        this.timer = new Timer();
        this.handlingTime = 0L;

        this.changeSupport = new PropertyChangeSupport(this);
    }

    public Task(UUID agentId, MediaRoutingDomain mrd, ChannelSession channelSession) {
        this(channelSession, mrd, null, new TaskState(Enums.TaskStateName.ACTIVE, null));
        this.assignedTo = agentId;
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

    /**
     * Sets the timeouts array for this task.
     *
     * @param timeouts the timeouts array
     */
    public void setTimeouts(List<Integer> timeouts) {
        this.timeouts = timeouts;
        if (timeouts.size() > 1) {
            this.startTimer();
        }
    }

    /**
     * Gets current step.
     *
     * @return the current step
     */
    public int getCurrentStep() {
        return currentStep;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
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
     * Add property change listener.
     *
     * @param listener the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(listener);
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

    /**
     * Remove property change listener.
     *
     * @param listener the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(listener);
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Schedules a Timer for a step for this task.
     */
    public void startTimer() {
        try {
            timer.schedule(new TaskTimer(), this.timeouts.get(currentStep++) * 1000L);
        } catch (IllegalArgumentException ex) {
            if (!"Negative delay.".equalsIgnoreCase(ExceptionUtils.getRootCause(ex).getMessage())) {
                LOGGER.error(ExceptionUtils.getMessage(ex));
                LOGGER.error(ExceptionUtils.getStackTrace(ex));
            }
        } catch (Exception ex) {
            LOGGER.error(ExceptionUtils.getMessage(ex));
            LOGGER.error(ExceptionUtils.getStackTrace(ex));
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
            LOGGER.debug("Time up for step: {}, Task id: {}, MRD: {}", Task.this.getCurrentStep(),
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
