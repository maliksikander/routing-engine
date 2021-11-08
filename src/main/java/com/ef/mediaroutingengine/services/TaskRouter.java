package com.ef.mediaroutingengine.services;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.commons.Constants;
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
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskRouter.class);
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
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
     * The Task manager.
     */
    private final TaskManager taskManager;
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
     * @param restRequest     to make rest calls to other components.
     * @param tasksRepository to communicate with the Redis Tasks collection.
     * @param jmsCommunicator the jms communicator
     */
    @Autowired
    public TaskRouter(AgentsPool agentsPool, RestRequest restRequest, TasksRepository tasksRepository,
                      JmsCommunicator jmsCommunicator, TaskManager taskManager) {
        this.agentsPool = agentsPool;
        this.restRequest = restRequest;
        this.tasksRepository = tasksRepository;
        this.jmsCommunicator = jmsCommunicator;
        this.taskManager = taskManager;
    }

    /**
     * Initializes the Scheduler.
     *
     * @param precisionQueue precision queue associated with this scheduler
     */
    public void init(PrecisionQueue precisionQueue) {
        if (!isInit) {
            this.precisionQueue = precisionQueue;
            this.taskManager.addPropertyChangeListener(Enums.EventName.NEW_TASK.name(), this);
            this.isInit = true;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.debug("TaskRouter for queue: [{}] invoked on event [{}]",
                precisionQueue.getName(), evt.getPropertyName());

        if (evt.getPropertyName().equals(Enums.EventName.NEW_TASK.name())) {
            this.onNewTask(evt);
        } else if (evt.getPropertyName().equals(Enums.EventName.STEP_TIMEOUT.name())) {
            this.onStepTimeout(evt);
        }

        if (precisionQueue.isEmpty()) {
            logger.debug("Queue [{}] is empty", this.precisionQueue.getName());
            return;
        }
        logger.debug("Queue [{}] is not empty", this.precisionQueue.getName());

        try {
            synchronized (precisionQueue.getServiceQueue()) {
                Task task = precisionQueue.peek();
                reserve(task);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.debug(Constants.METHOD_ENDED);
    }

    /**
     * Listen to task property changes if new task event fired.
     *
     * @param evt the evt
     */
    private void onNewTask(PropertyChangeEvent evt) {
        Task task = (Task) evt.getNewValue();
        if (task.getQueue().equals(this.precisionQueue.getId())) {
            this.precisionQueue.enqueue(task);
            logger.debug("Task: {} enqueued in Precision-Queue: {}", task.getId(), precisionQueue.getId());
            task.addPropertyChangeListener(Enums.EventName.STEP_TIMEOUT.name(), this);
            task.setUpStepFrom(this.precisionQueue, 0);
        }
    }

    private void onStepTimeout(PropertyChangeEvent evt) {
        Task task = (Task) evt.getNewValue();
        int currentStepIndex = this.precisionQueue.getStepIndex(task.getCurrentStep().getStep());
        task.setUpStepFrom(this.precisionQueue, currentStepIndex + 1);
    }

    private void reserve(Task task) {
        boolean assignedToLastAssignedAgent = this.assignToLastAssignedAgent(task);
        if (!assignedToLastAssignedAgent) {
            int currentStepIndex = precisionQueue.getStepIndex(task.getCurrentStep().getStep());
            for (int i = 0; i < currentStepIndex + 1; i++) {
                Step step = precisionQueue.getStepAt(i);
                logger.info("Step: {} searching in queue: {}", i, precisionQueue.getName());
                Agent agent = this.getAvailableAgentWithLeastActiveTasks(step);
                if (agent != null) {
                    logger.debug("Agent: {} is available to schedule task: {}", agent.getId(), task.getId());
                    this.assignTaskTo(agent, task);
                    return;
                }
            }
            logger.debug("Could not find an agent at the moment for task: {}", task.getId());
        }
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
     * Assign task to.
     *
     * @param agent the agent
     * @param task  the task
     */
    private void assignTaskTo(Agent agent, Task task) {
        logger.debug("method started");
        try {
            if (task.isAgentRequestTimeout()) {
                logger.debug("AgentRequestTtlTimeout method returning..");
                return;
            }
            CCUser ccUser = agent.toCcUser();
            boolean isReserved = true;
            //this.restRequest.postAssignTask(task.getChannelSession(), ccUser, task.getTopicId(), task.getId());
            if (isReserved) {
                logger.debug("Task Assigned to agent in Agent-Manager");
                this.changeStateOf(task, new TaskState(Enums.TaskStateName.RESERVED, null), agent.getId());
//                this.jmsCommunicator.publishTaskStateChangeForReporting(task);
                precisionQueue.dequeue();
                agent.reserveTask(task);
                this.restRequest.postAgentReserved(task.getTopicId(), ccUser);

                task.getTimer().cancel();
                task.removePropertyChangeListener(Enums.EventName.STEP_TIMEOUT.name(), this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("method ended");
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