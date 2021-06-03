package com.ef.mediaroutingengine.services;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentSelectionCriteria;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Step;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import com.ef.mediaroutingengine.services.redis.TaskDao;
import com.ef.mediaroutingengine.services.utilities.RestRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TaskScheduler implements PropertyChangeListener {
    // Queue Scheduler works as short term scheduler,
    // it scans the queue and removes task to assign an agent

    /*
    This properties change listener is listening on the following property changes:
    Agent-state: whenever Agent state changes to READY or ACTIVE
    TaskServiceManager: NewTask, TaskState
    Task: Timer, RemoveTask,
     */

    // Observer pattern


    private static final Logger LOGGER = LoggerFactory.getLogger(TaskScheduler.class);
    private final TasksPool tasksPool;
    private final AgentsPool agentsPool;
    private final RestRequest restRequest;
    private final TaskDao taskDao;

    private String name;
    private PrecisionQueue precisionQueue;
    private boolean isInit;

    /**
     * Constructor.
     *
     * @param tasksPool the pool of all tasks
     * @param agentsPool the pool of all agents
     * @param restRequest to make rest calls to other components.
     * @param taskDao to communicate with the Redis Tasks collection.
     */
    @Autowired
    public TaskScheduler(TasksPool tasksPool, AgentsPool agentsPool, RestRequest restRequest,
                         TaskDao taskDao) {
        this.tasksPool = tasksPool;
        this.agentsPool = agentsPool;
        this.restRequest = restRequest;
        this.taskDao = taskDao;
    }

    /**
     * Initializes the Scheduler.
     *
     * @param name           the name of the scheduler
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
        this.tasksPool.addPropertyChangeListener(this, this.name); // for new Task
    }

    public void subscribeTaskPropertyChangeSupportAfterFailover(Task task) {
        task.addPropertyChangeListener(this);
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

            Task task = precisionQueue.peek();

            synchronized (precisionQueue.getServiceQueue()) {
                assign(task);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        LOGGER.debug("Property changed");
    }

    private void assign(Task task) {
//        boolean assignedToLastAssignedAgent = this.assignToLastAssignedAgent(task);
        boolean assignedToLastAssignedAgent = false;
        if (!assignedToLastAssignedAgent) {
            boolean isReserved = false;

            for (int i = 0; i < task.getCurrentStep() + 1; i++) {
                Step step = precisionQueue.getStepAt(i);
                LOGGER.info("Step: {} searching in queue: {}", i, precisionQueue.getName());

                int leastNoOfTasks = getLowestNumberOfTasksAnAvailableAgentHasIn(step);
                List<Agent> associatedAgents = step.orderAgentsBy(AgentSelectionCriteria.LONGEST_AVAILABLE);

                Agent agent = this.findAvailableAgentWithLeastTasks(associatedAgents, leastNoOfTasks);
                if (agent != null) {
                    this.assignTaskTo(agent, task, false);
                    isReserved = true;
                    break;
                }
            }

            if (!isReserved) {
                this.changeStateOf(task, Enums.TaskStateName.QUEUED, Enums.TaskStateReasonCode.NONE);
            }
        }
    }

    private void listenToTaskPropertyChangesIfNewTaskEventFired(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.NEW_TASK.name())) {
            Task task = (Task) evt.getNewValue();
            task.addPropertyChangeListener(Enums.EventName.TIMER.name(), this);
        }
    }

    private void startNextStepTimerIfTimerEventFired(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.TIMER.name())) {
            Task task = (Task) evt.getNewValue();
            int currentStep = task.getCurrentStep();

            if (currentStep + 1 < precisionQueue.getSteps().size()) {
                task.startTimer();
            }
            LOGGER.debug("step {} associated agents search for available agent", currentStep + 1);
        }
    }

    private boolean isActiveOrReadyAndIsRoutable(Agent agent) {
        return (agent.getState() == Enums.AgentState.ACTIVE
                || agent.getState() == Enums.AgentState.READY)
                && agent.getAgentMode() == Enums.AgentMode.ROUTABLE;
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

    private boolean assignToLastAssignedAgent(Task task) {
        UUID lastAssignedAgentId = task.getLastAssignedAgentId();
        if (lastAssignedAgentId != null) {
            Agent agent = this.agentsPool.findById(lastAssignedAgentId);
            if (agent != null && isActiveOrReadyAndIsRoutable(agent)) {
                assignTaskTo(agent, task, true);
                return true;
            }
        }
        return false;
    }

    private Agent findAvailableAgentWithLeastTasks(List<Agent> agents, int lowestTasks) {
        for (Agent agent : agents) {
            if (isActiveOrReadyAndIsRoutableAndHasLowestTasks(agent, lowestTasks)) {
                return agent;
            }
        }
        return null;
    }

    private boolean assignTaskTo(Agent agent, Task task, boolean handleTaskRemoveEvent) {
        try {
            CCUser ccUser = agent.toCcUser();
            boolean isAssigned = this.restRequest.postAssignTask(task.getChannelSession(),
                    ccUser, task.getTopicId(), task.getId());
            if (isAssigned) {
                this.changeStateOf(task, Enums.TaskStateName.RESERVED, Enums.TaskStateReasonCode.NONE);
                precisionQueue.dequeue();
                agent.assignTask(task);

                if (handleTaskRemoveEvent) {
                    task.handleTaskRemoveEvent();
                }
                task.removePropertyChangeListener(Enums.EventName.TIMER.name(), this);

                this.restRequest.postAgentReserved(task.getTopicId(), ccUser);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void changeStateOf(Task task, Enums.TaskStateName taskStateName, Enums.TaskStateReasonCode reasonCode) {
        if (!task.getTaskState().equals(taskStateName)) {
            task.setTaskState(new TaskState(taskStateName, reasonCode));
            this.taskDao.changeState(task.getId(), taskStateName);
        }
    }
}