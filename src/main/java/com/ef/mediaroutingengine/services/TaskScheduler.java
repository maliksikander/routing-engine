package com.ef.mediaroutingengine.services;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentSelectionCriteria;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Step;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
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
    private final TasksRepository tasksRepository;

    private PrecisionQueue precisionQueue;
    private boolean isInit;

    /**
     * Constructor.
     *
     * @param tasksPool       the pool of all tasks
     * @param agentsPool      the pool of all agents
     * @param restRequest     to make rest calls to other components.
     * @param tasksRepository to communicate with the Redis Tasks collection.
     */
    @Autowired
    public TaskScheduler(TasksPool tasksPool, AgentsPool agentsPool, RestRequest restRequest,
                         TasksRepository tasksRepository) {
        this.tasksPool = tasksPool;
        this.agentsPool = agentsPool;
        this.restRequest = restRequest;
        this.tasksRepository = tasksRepository;
    }

    /**
     * Initializes the Scheduler.
     *
     * @param name           the name of the scheduler
     * @param precisionQueue precision queue associated with this scheduler
     */
    public void init(String name, PrecisionQueue precisionQueue) {
        if (!isInit) {
            name = name + " task scheduler";
            this.precisionQueue = precisionQueue;
            this.tasksPool.addPropertyChangeListener(this, name); // for new Task
            this.isInit = true;
        }
    }

    public void subscribeTaskPropertyChangeSupportAfterFailover(Task task) {
        task.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LOGGER.debug("TaskScheduler for queue {} invoked on event {}", precisionQueue.getName(), evt.getPropertyName());
        try {
            this.listenToTaskPropertyChangesIfNewTaskEventFired(evt);
            this.startNextStepTimerIfTimerEventFired(evt);

            if (precisionQueue.isEmpty()) {
                return;
            }
            LOGGER.debug("Precision-Queue is not empty | TaskScheduler.propertyChange method");

            Task task = precisionQueue.peek();
            LOGGER.debug("Queue.peek: Task: {} | TaskScheduler.propertyChange method", task.getId());
            synchronized (precisionQueue.getServiceQueue()) {
                reserve(task);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        LOGGER.debug("Property changed");
    }

    private void reserve(Task task) {
        LOGGER.debug("method started | TaskScheduler.reserve method");
//        boolean assignedToLastAssignedAgent = this.assignToLastAssignedAgent(task);
        boolean assignedToLastAssignedAgent = false;
        if (!assignedToLastAssignedAgent) {
            for (int i = 0; i < task.getCurrentStep() + 1; i++) {
                Step step = precisionQueue.getStepAt(i);
                LOGGER.info("Step: {} searching in queue: {}", i, precisionQueue.getName());
                Agent agent = this.getAvailableAgentWithLeastActiveTasks(step);
                if (agent != null) {
                    LOGGER.debug("Agent: {} is available to schedule task | TaskScheduler.reserve method",
                            agent.getId());
                    this.assignTaskTo(agent, task);
                    break;
                }
            }
        }
        LOGGER.debug("method ended | TaskScheduler.reserve method");
    }

    private void listenToTaskPropertyChangesIfNewTaskEventFired(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.NEW_TASK.name())) {
            Task task = (Task) evt.getNewValue();
            task.addPropertyChangeListener(Enums.EventName.TIMER.name(), this);
            task.addPropertyChangeListener(Enums.EventName.TASK_REMOVED.name(), this);
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

    private boolean isAvailable(Agent agent) {
        UUID mrdId = this.precisionQueue.getMrd().getId();
        Enums.AgentStateName agentState = agent.getState().getName();
        Enums.AgentMrdStateName mrdState = agent.getAgentMrdState(mrdId).getState();

        // (Agent State is ready) AND (AgnetMRrdState is ready OR active) AND (No task is reserved for this agent)
        // Only one task can be *reserved* for an Agent at a time.
        return agentState.equals(Enums.AgentStateName.READY)
                && (mrdState.equals(Enums.AgentMrdStateName.ACTIVE)
                || mrdState.equals(Enums.AgentMrdStateName.READY))
                && !agent.isTaskReserved();
    }

    private Agent getAvailableAgentWithLeastActiveTasks(Step step) {
        List<Agent> sortedAgentList = step.orderAgentsBy(AgentSelectionCriteria.LONGEST_AVAILABLE,
                this.precisionQueue.getMrd().getId());
        int lowestNumberOfTasks = Integer.MAX_VALUE;
        Agent result = null;
        for (Agent agent : sortedAgentList) {
            int noOfTasksOnMrd = agent.getNoOfActiveTasks(this.precisionQueue.getMrd().getId());
            if (isAvailable(agent) && noOfTasksOnMrd < lowestNumberOfTasks) {
                lowestNumberOfTasks = noOfTasksOnMrd;
                result = agent;
            }
        }
        return result;
    }

    private boolean assignToLastAssignedAgent(Task task) {
        UUID lastAssignedAgentId = task.getLastAssignedAgentId();
        if (lastAssignedAgentId != null) {
            Agent agent = this.agentsPool.findById(lastAssignedAgentId);
            if (agent != null && isAvailable(agent)) {
                assignTaskTo(agent, task);
                return true;
            }
        }
        return false;
    }

    private void assignTaskTo(Agent agent, Task task) {
        LOGGER.debug("method started | TaskScheduler.assignTaskTo method");
        try {
            if (task.isAgentRequestTimeout()) {
                LOGGER.debug("AgentRequestTtlTimeout method returning.. | TaskScheduler.assignTaskTo method");
                return;
            }
            CCUser ccUser = agent.toCcUser();
            boolean isReserved = this.restRequest.postAssignTask(task.getChannelSession(), ccUser,
                    task.getTopicId(), task.getId());
            if (isReserved) {
                LOGGER.debug("Task Assigned to agent in Agent-Manager | TaskScheduler.assignTaskTo method");
                this.changeStateOf(task, new TaskState(Enums.TaskStateName.RESERVED, null), agent.getId());
                precisionQueue.dequeue();
                agent.reserveTask(task);
                this.restRequest.postAgentReserved(task.getTopicId(), ccUser);
//                task.handleTaskRemoveEvent();
                task.removePropertyChangeListener(Enums.EventName.TIMER.name(), this);
                task.removePropertyChangeListener(Enums.EventName.TASK_REMOVED.name(), this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.debug("method ended | TaskScheduler.assignTaskTo method");
    }

    private void changeStateOf(Task task, TaskState state, UUID agentId) {
        task.setTaskState(state);
        this.tasksRepository.changeState(task.getId(), state);
        task.setAssignedTo(agentId);
        this.tasksRepository.updateAssignedTo(task.getId(), agentId);
    }
}