package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.eventlisteners.DispatchEWT;
import com.ef.mediaroutingengine.eventlisteners.EwtRequestEvent;
import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
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
    private final PrecisionQueue queue;
    private final String selectedPriorityLabel;

    private int priority;
    private Enums.TaskState taskState;
    private UUID assignedTo;

    private final Long enqueueTime;
    private Long startTime;
    private Long handlingTime;

    private final Timer timer;
    private int[] timeouts;
    private int currentStep;

    private final PropertyChangeSupport changeSupport;

    /**
     * Default constructor.
     *
     * @param channelSession the channel session in request
     * @param mrd            associated media routing domain
     * @param priorityLabel  priority label
     */
    public Task(ChannelSession channelSession, MediaRoutingDomain mrd, PrecisionQueue queue,
                String priorityLabel) {
        this.id = UUID.randomUUID();
        this.channelSession = channelSession;
        this.mrd = mrd;
        this.queue = queue;
        this.selectedPriorityLabel = priorityLabel;

        this.priority = channelSession.getChannelData().getRequestPriority();
        this.taskState = Enums.TaskState.CREATED;
        this.enqueueTime = System.currentTimeMillis();
        this.timer = new Timer();

        this.changeSupport = new PropertyChangeSupport(this);
        this.changeSupport.addPropertyChangeListener(new DispatchEWT());
        this.changeSupport.addPropertyChangeListener(new EwtRequestEvent());
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

    public PrecisionQueue getQueue() {
        return this.queue;
    }

    public String getSelectedPriorityLabel() {
        return selectedPriorityLabel;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Enums.TaskState getTaskState() {
        return this.taskState;
    }

    public void setTaskState(Enums.TaskState state) {
        this.taskState = state;
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
    public void setTimeouts(int[] timeouts) {
        this.timeouts = timeouts;
        if (timeouts.length > 1) {
            this.startTimer();
        }
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public String getCustomerName() {
        return this.channelSession.getLinkedCustomer().getAssociatedCustomer().getFirstName();
    }

    public UUID getTopicId() {
        return this.channelSession.getTopicId();
    }

    public UUID getLastAssignedAgentId() {
        return this.channelSession.getLinkedCustomer().getLastAssignedAgent();
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
            timer.schedule(new TaskTimer(), this.timeouts[currentStep++] * 1000L);
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

    public void handleEvent(Enums.EventName property, JsonNode node) {
        this.changeSupport.firePropertyChange(property.name(), null, node);
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
