package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.ChannelSession;
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

public class Task implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    private final UUID id;
    private final ChannelSession channelSession;
    private final MediaRoutingDomain mrd;
    private final UUID queue;
    private final Timer timer;
    private final PropertyChangeSupport changeSupport;
    private int priority;
    private TaskState state;
    private UUID assignedTo;
    private Long enqueueTime;
    private Long startTime;
    private Long handlingTime;
    private List<Integer> timeouts;
    private int currentStep;

    /**
     * Default constructor.
     *
     * @param channelSession the channel session in request
     * @param mrd            associated media routing domain
     */
    public Task(ChannelSession channelSession, MediaRoutingDomain mrd, UUID queue) {
        this.id = UUID.randomUUID();
        this.channelSession = channelSession;
        this.mrd = mrd;
        this.queue = queue;

        this.priority = 10; // Right now hardcoded at highest priority level
        this.state = new TaskState(Enums.TaskStateName.QUEUED, null);
        this.enqueueTime = System.currentTimeMillis();
        this.timer = new Timer();
        this.handlingTime = 0L;

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

        this.priority = o.getPriority();
        this.state = new TaskState(Enums.TaskStateName.QUEUED, null);
        this.timer = new Timer();
        this.handlingTime = 0L;

        this.changeSupport = new PropertyChangeSupport(this);
    }

    // +++++++++++++++++++++++++++++++ Accessor Methods ++++++++++++++++++++++++++++++++++++++++++++++

    public UUID getId() {
        return this.id;
    }

    public ChannelSession getChannelSession() {
        return channelSession;
    }

    public MediaRoutingDomain getMrd() {
        return this.mrd;
    }

    public UUID getQueue() {
        return this.queue;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public TaskState getTaskState() {
        return this.state;
    }

    public void setTaskState(TaskState state) {
        this.state = state;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Long getEnqueueTime() {
        return enqueueTime;
    }

    public void setEnqueueTime(Long enqueueTime) {
        this.enqueueTime = enqueueTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getHandlingTime() {
        return handlingTime;
    }

    public void setHandlingTime(Long handlingTime) {
        this.handlingTime = handlingTime + this.handlingTime;
    }

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

    public int getCurrentStep() {
        return currentStep;
    }

    public UUID getTopicId() {
        return this.channelSession.getTopicId();
    }

    // TODO: Implement it correctly
    public UUID getLastAssignedAgentId() {
        return UUID.randomUUID();
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(property, listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(property, listener);
    }

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

    public void handleTaskRemoveEvent() {
        this.changeSupport.firePropertyChange(Enums.EventName.TASK_REMOVED.name(), null, "");
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
        String result = "";
        result = "TaskId: " + this.id + ", MRD: " + this.mrd + ", Priority: " + this.getPriority();
        return result;
    }

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
