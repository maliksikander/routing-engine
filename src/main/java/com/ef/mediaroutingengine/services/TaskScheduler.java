package com.ef.mediaroutingengine.services;

import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentSelectionCriteria;
import com.ef.mediaroutingengine.model.CommonEnums;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Step;
import com.ef.mediaroutingengine.model.TaskService;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskScheduler implements PropertyChangeListener {
    // Queue Scheduler works as short term scheduler,
    // it scans the queue and removes task to assign an agent

    /*
    This properties change listener is listening on the following property changes:
    Agent-state: whenever Agent state changes to READY or ACTIVE
    TaskServiceManager: NewTask, TaskState
    Task: Timer.
     */

    // Observer pattern


    private static final Logger LOGGER = LoggerFactory.getLogger(TaskScheduler.class);
    private final TaskServiceManager taskServiceManager;
    private final AgentsPool agentsPool;

    private String name;
    private PrecisionQueue precisionQueue;
    private boolean isInit;

    @Autowired
    public TaskScheduler(TaskServiceManager taskServiceManager, AgentsPool agentsPool) {
        this.taskServiceManager = taskServiceManager;
        this.agentsPool = agentsPool;
    }

    /**
     * Initializes the Scheduler.
     *
     * @param name the name of the scheduler
     * @param precisionQueue precision queue associated with this scheduler
     */
    public void init(String name, PrecisionQueue precisionQueue) {
        if (!isInit) {
            this.name = name + " task scheduler";
            this.precisionQueue = precisionQueue;
            subscribeToProperties();
            this.isInit = true;
        }
    }

    /**
     * Subscribes to property change events, All agent state change events and Task Service manager events.
     */
    public void subscribeToProperties() {
        for (Step step : precisionQueue.getSteps()) {
            for (Agent agent : step.getAssociatedAgents()) {
                agent.addSchedulerListener(this, this.name);
            }
        }
        this.taskServiceManager.addPropertyChangeListener(this, this.name); // for new Task
    }

    public void subscribeTaskPropertyChangeSupportAfterFailover(TaskService taskService) {
        taskService.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LOGGER.debug("TaskScheduler for queue {} invoked on event {}",
                precisionQueue.getName(), evt.getPropertyName());
        try {
            this.listenToTaskPropertyChangesIfNewTaskEventFired(evt);
            this.startNextStepTimerIfTimerEventFired(evt);

            if (precisionQueue.isEmpty()) {
                return;
            }

            TaskService task = precisionQueue.peek();

            synchronized (precisionQueue.getServiceQueue()) {
                assign(task);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        LOGGER.debug("Property changed");
    }

    private void assign(TaskService task) {
        boolean assignedToLastAssignedAgent = this.assignToLastAssignedAgent(task);

        if (!assignedToLastAssignedAgent) {
            for (int i = 0; i < task.getCurrentStep() + 1; i++) {
                Step step = precisionQueue.getStepAt(i);
                LOGGER.info("Step: {} searching in queue: {}", i, precisionQueue.getName());

                int leastNoOfTasks = getLowestNumberOfTasksAnAvailableAgentHasIn(step);
                List<Agent> associatedAgents = step.orderAgentsBy(AgentSelectionCriteria.LONGEST_AVAILABLE);

                if (this.assignToAvailableAgentWithLeastTasks(associatedAgents, task, leastNoOfTasks)) {
                    break;
                }
            }
        }
    }

    private void listenToTaskPropertyChangesIfNewTaskEventFired(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.EventProperties.NEW_TASK.name())) {
            TaskService task = (TaskService) evt.getNewValue();
            task.addPropertyChangeListener(this);
        }
    }

    private void startNextStepTimerIfTimerEventFired(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.EventProperties.TIMER.name())) {
            TaskService task = (TaskService) evt.getNewValue();
            int currentStep = task.getCurrentStep();

            if (currentStep + 1 < precisionQueue.getSteps().size()) {
                task.startTimer();
            }
            LOGGER.debug("step {} associated agents search for available agent", currentStep + 1);
        }
    }

    private boolean isActiveOrReadyAndIsRoutable(Agent agent) {
        return (agent.getState() == CommonEnums.AgentState.ACTIVE
                || agent.getState() == CommonEnums.AgentState.READY)
                && agent.getAgentMode() == CommonEnums.AgentMode.ROUTABLE;
    }

    private boolean isActiveOrReadyAndIsRoutableAndHasLowestTasks(Agent agent, int lowestTasks) {
        return isActiveOrReadyAndIsRoutable(agent) && agent.getNumOfTasks() == lowestTasks;
    }

    private int getLowestNumberOfTasksAnAvailableAgentHasIn(Step step) {
        int lowestNumberOfTasks = Integer.MAX_VALUE;
        for (Agent agent : step.getAssociatedAgents()) {
            if (isActiveOrReadyAndIsRoutable(agent) && agent.getNumOfTasks() < lowestNumberOfTasks) {
                lowestNumberOfTasks = agent.getNumOfTasks();
            }
        }
        return lowestNumberOfTasks;
    }

    private boolean assignToLastAssignedAgent(TaskService task) {
        String lastAssignedAgentId = task.getLastAssignedAgentId();
        if (lastAssignedAgentId != null && !lastAssignedAgentId.isEmpty()) {
            Agent agent = this.agentsPool.findById(UUID.fromString(lastAssignedAgentId));
            if (agent != null && isActiveOrReadyAndIsRoutable(agent)) {
                assignTaskTo(agent, task, true);
                return true;
            }
        }
        return false;
    }

    private boolean assignToAvailableAgentWithLeastTasks(List<Agent> agents, TaskService task, int lowestTasks) {
        for (Agent agent : agents) {
            if (isActiveOrReadyAndIsRoutableAndHasLowestTasks(agent, lowestTasks)) {
                this.assignTaskTo(agent, task, false);
                return true;
            }
        }
        return false;
    }

    private void assignTaskTo(Agent agent, TaskService task, boolean handleTaskRemoveEvent) {
        agent.assignTask(task.getId());
        precisionQueue.dequeue();
        if (handleTaskRemoveEvent) {
            task.handleTaskRemoveEvent();
        }
        task.removePropertyChangeListener(this);
        this.taskServiceManager.dispatchSelectedAgent(agent, task);
    }
}