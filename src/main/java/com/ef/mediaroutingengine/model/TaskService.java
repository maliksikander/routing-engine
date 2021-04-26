package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.eventlisteners.DispatchEWT;
import com.ef.mediaroutingengine.eventlisteners.EwtRequestEvent;
import com.ef.mediaroutingengine.repositories.MrdPool;
import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskService {
    private String id;
    private String customerName;
    private MediaRoutingDomain mrd;
    private String queueName;
    private List<PropertyChangeListener> listeners;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private Timer timer = new Timer();
    private int[] timeouts;
    private int currentStep;
    private String conversationId;
    private Long startTime;
    private Long enqueueTime;
    private Long handlingTime;
    private CommonEnums.TaskState taskState;
    private int priority = 1;
    private String selectedPriorityLabel = "";
    private String lastAssignedAgentId = "";
    private UUID assignedTo;

    protected static Logger log = LogManager.getLogger(TaskService.class.getName());

    /**
     * Parameterized constructor.
     *
     * @param id id of the task service
     * @param customerName name of the customer
     * @param mrdName name of MRD
     */
    public TaskService(String id, String customerName, String mrdName) {
        this.id = id;
        this.customerName = customerName;
        this.mrd = MrdPool.getInstance().getMrd(mrdName);
        this.currentStep = 0;
        this.initialize();
    }

    public TaskService(String id) {
        this.id = id;
        this.initialize();
    }

    /**
     * Parameterized constructor.
     *
     * @param id id of this task service object
     * @param customerName name of customer
     * @param mrdName name of MRD
     * @param priority priority of this task
     * @param priorityLabel priority label associated with the priority
     * @param lastAssignedAgentId id of the last assigned agent to this task if any
     * @param conversationId the conversation id for this task
     */
    public TaskService(String id, String customerName, String mrdName, int priority, String priorityLabel,
                       String lastAssignedAgentId, String conversationId) {
        this(id, customerName, mrdName);
        this.priority = priority;
        this.selectedPriorityLabel = priorityLabel;
        this.lastAssignedAgentId = lastAssignedAgentId;
        this.conversationId = conversationId;
        this.enqueueTime = System.currentTimeMillis();
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return this.queueName;
    }

    private void initialize() {
        this.taskState = CommonEnums.TaskState.NEW;
        this.handlingTime = 0L;
        this.listeners = new LinkedList<>();

        //add commands
        listeners.add(new DispatchEWT());

        //add events
        listeners.add(new EwtRequestEvent());

        //add task scheduler listener
        for (PropertyChangeListener listener : this.listeners) {
            this.changeSupport.addPropertyChangeListener(listener);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(listener);
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

    public int[] getTimeouts() {
        return this.timeouts;
    }

    /**
     * Schedules a Timer for a step for this task.
     */
    public void startTimer() {
        if (timeouts != null && timer != null) {
            timer = new Timer();
        }
        //if(this.timeouts[currentStep] != -1)
        try {
            timer.schedule(new TaskServiceTimer(), this.timeouts[currentStep++] * 1000);
        } catch (IllegalArgumentException ex) {
            if (!"Negative delay.".equalsIgnoreCase(ExceptionUtils.getRootCause(ex).getMessage())) {
                log.error(ExceptionUtils.getMessage(ex));
                log.error(ExceptionUtils.getStackTrace(ex));
            }
        } catch (Exception ex) {
            log.error(ExceptionUtils.getMessage(ex));
            log.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    public Timer getTimer() {
        return this.timer;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public MediaRoutingDomain getMrd() {
        return this.mrd;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    public void handleEvent(CommonEnums.EventProperties property, JsonNode node) {
        this.changeSupport.firePropertyChange(property.name(), null, node);
    }

    public void handleTimeoutEvent() {
        this.changeSupport.firePropertyChange("Timer", null, this);
    }

    public void handleTaskRemoveEvent() {
        this.changeSupport.firePropertyChange("TaskRemoved", null, "");
    }


    protected class TaskServiceTimer extends TimerTask {

        public void run() {
            log.debug("Time up for step: {}, Task id: {}, MRD: {}", TaskService.this.getCurrentStep(),
                    TaskService.this.getId(), TaskService.this.getMrd().getName());
            try {
                TaskService.this.getTimer().cancel();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            TaskService.this.handleTimeoutEvent();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskService that = (TaskService) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String result = "";
        result = "TaskId: " + this.id + ", MRD: " + this.mrd.getName() + ", Customer Name: " + this.customerName
                + ", Priority: " + this.priority;
        return result;
    }

    public void setTaskState(CommonEnums.TaskState state) {
        this.taskState = state;
    }

    public CommonEnums.TaskState getTaskState() {
        return this.taskState;
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

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getEnqueueTime() {
        return enqueueTime;
    }

    public void setEnqueueTime(Long enqueueTime) {
        this.enqueueTime = enqueueTime;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getLastAssignedAgentId() {
        return lastAssignedAgentId;
    }

    public void setLastAssignedAgentId(String lastAssignedAgentId) {
        this.lastAssignedAgentId = lastAssignedAgentId;
    }

    public String getSelectedPriorityLabel() {
        return selectedPriorityLabel;
    }

    public void setSelectedPriorityLabel(String selectedPriorityLabel) {
        this.selectedPriorityLabel = selectedPriorityLabel;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }
}
