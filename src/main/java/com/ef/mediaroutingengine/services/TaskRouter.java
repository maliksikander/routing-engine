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
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.utilities.RestRequest;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
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

/**
 * The type Task scheduler.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TaskRouter implements PropertyChangeListener {
    // Queue Scheduler works as short term scheduler,
    // it scans the queue and removes task to assign an agent

    /*
    This property change listener is listening on the following property changes:
    Agent-state: whenever Agent state changes to READY or ACTIVE
    TaskServiceManager: NewTask, TaskState
    Task: Timer, RemoveTask,
     */

    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskRouter.class);
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;
    /**
     * The Rest request.
     */
    private final RestRequest restRequest;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Jms communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    /**
     * The Precision queue.
     */
    private PrecisionQueue precisionQueue;
    /**
     * The Is init.
     */
    private boolean isInit;

    /**
     * Constructor.
     *
     * @param agentsPool      the pool of all agents
     * @param taskManager     the task manager
     * @param restRequest     to make rest calls to other components.
     * @param tasksRepository to communicate with the Redis Tasks collection.
     * @param jmsCommunicator the jms communicator
     */
    @Autowired
    public TaskRouter(AgentsPool agentsPool, TaskManager taskManager,
                      RestRequest restRequest, TasksRepository tasksRepository,
                      JmsCommunicator jmsCommunicator) {
        this.agentsPool = agentsPool;
        this.taskManager = taskManager;
        this.restRequest = restRequest;
        this.tasksRepository = tasksRepository;
        this.jmsCommunicator = jmsCommunicator;
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
            this.taskManager.addPropertyChangeListener(this, name); // for new Task
            this.isInit = true;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.debug("TaskScheduler for queue {} invoked on event {}", precisionQueue.getName(), evt.getPropertyName());
        try {
            this.listenToTaskPropertyChangesIfNewTaskEventFired(evt);
            this.startNextStepTimerIfTimerEventFired(evt);

            if (precisionQueue.isEmpty()) {
                return;
            }
            logger.debug("Precision-Queue is not empty | TaskScheduler.propertyChange method");

            synchronized (precisionQueue.getServiceQueue()) {
                Task task = precisionQueue.peek();
                logger.debug("Queue.peek: Task: {} | TaskScheduler.propertyChange method", task.getId());
                reserve(task);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.debug("Property changed");
    }

    private void reserve(Task task) {
        // boolean assignedToLastAssignedAgent = this.assignToLastAssignedAgent(task);
        boolean assignedToLastAssignedAgent = false;
        if (!assignedToLastAssignedAgent) {
            int currentStepIndex = precisionQueue.getStepIndex(task.getCurrentStep());
            for (int i = 0; i < currentStepIndex + 1; i++) {
                Step step = precisionQueue.getStepAt(i);
                logger.info("Step: {} searching in queue: {}", i, precisionQueue.getName());
                Agent agent = this.getAvailableAgentWithLeastActiveTasks(step);
                if (agent != null) {
                    logger.debug("Agent: {} is available to schedule task: {}", agent.getId(), task.getId());
                    this.assignTaskTo(agent, task);
                    break;
                }
            }
            logger.debug("Could not find an agent at the moment for task: {}", task.getId());
        }
    }

    /**
     * Listen to task property changes if new task event fired.
     *
     * @param evt the evt
     */
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
            logger.debug("Task Timer event fired for task: {}", task.getId());

            int currentStepIndex = this.precisionQueue.getStepIndex(task.getCurrentStep());
            int nextStepIndex = currentStepIndex + 1;

            task.setCurrentStep(precisionQueue.getStepAt(nextStepIndex));

            // If next step is not the last step, start step expiry timer in the task
            if (nextStepIndex < precisionQueue.getSteps().size() - 1) {
                logger.debug("STEP IS NOT THE LAST STEP TASK TIMER IS STARTING");
                task.startTimer();
            }
            logger.debug("step {} associated agents search for available agent", currentStepIndex + 1);
        }
    }

    /**
     * Gets available agent with the least number of active tasks.
     *
     * @param step the step
     * @return the available agent with the least number of active tasks
     */
    private Agent getAvailableAgentWithLeastActiveTasks(Step step) {
        List<Agent> sortedAgentList = step.orderAgentsBy(AgentSelectionCriteria.LONGEST_AVAILABLE,
                this.precisionQueue.getMrd().getId());
        int lowestNumberOfTasks = Integer.MAX_VALUE;
        Agent result = null;
        for (Agent agent : sortedAgentList) {

            String mrdId = this.precisionQueue.getMrd().getId();
            int noOfTasksOnMrd = agent.getNoOfActivePushTasks(mrdId);

            if (agent.isAvailableForRouting(mrdId) && noOfTasksOnMrd < lowestNumberOfTasks) {
                lowestNumberOfTasks = noOfTasksOnMrd;
                result = agent;
            }
        }
        return result;
    }

    /**
     * Assign to last assigned agent boolean.
     *
     * @param task the task
     * @return the boolean
     */
    private boolean assignToLastAssignedAgent(Task task) {
        UUID lastAssignedAgentId = task.getLastAssignedAgentId();
        if (lastAssignedAgentId != null) {
            Agent agent = this.agentsPool.findById(lastAssignedAgentId);
            String mrdId = this.precisionQueue.getMrd().getId();
            if (agent != null && agent.isAvailableForRouting(mrdId)) {
                assignTaskTo(agent, task);
                return true;
            }
        }
        return false;
    }

    /**
     * Assign task to.
     *
     * @param agent the agent
     * @param task  the task
     */
    private void assignTaskTo(Agent agent, Task task) {
        logger.debug("method started | TaskScheduler.assignTaskTo method");
        try {
            if (task.isAgentRequestTimeout()) {
                logger.debug("AgentRequestTtlTimeout method returning.. | TaskScheduler.assignTaskTo method");
                return;
            }
            CCUser ccUser = agent.toCcUser();
            boolean isReserved = this.restRequest.postAssignTask(task.getChannelSession(), ccUser,
                    task.getTopicId(), task.getId());
            if (isReserved) {
                logger.debug("Task Assigned to agent in Agent-Manager | TaskScheduler.assignTaskTo method");
                this.changeStateOf(task, new TaskState(Enums.TaskStateName.RESERVED, null), agent.getId());
                this.jmsCommunicator.publishTaskStateChangeForReporting(task);
                precisionQueue.dequeue();
                agent.reserveTask(task);
                this.restRequest.postAgentReserved(task.getTopicId(), ccUser);
//                task.handleTaskRemoveEvent();
                task.getTimer().cancel();
                task.removePropertyChangeListener(Enums.EventName.TIMER.name(), this);
                task.removePropertyChangeListener(Enums.EventName.TASK_REMOVED.name(), this);
            }
        } catch (Exception e) {
            // Todo: Apache Commons: Use ExceptionUtils instead of printStackTrace.
            e.printStackTrace();
        }
        logger.debug("method ended | TaskScheduler.assignTaskTo method");
    }

    /**
     * Change state of.
     *
     * @param task    the task
     * @param state   the state
     * @param agentId the agent id
     */
    private void changeStateOf(Task task, TaskState state, UUID agentId) {
        task.setTaskState(state);
        this.tasksRepository.changeState(task.getId(), state);
        task.setAssignedTo(agentId);
        this.tasksRepository.updateAssignedTo(task.getId(), agentId);
    }
}