package com.ef.mediaroutingengine.routing;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.AgentSelectionCriteria;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.model.Step;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.routing.utility.TaskUtility;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
     * The Rest Request Class Object.
     */
    private final RestRequest restRequest;

    /**
     * Constructor.
     *
     * @param agentsPool      the pool of all agents
     * @param tasksRepository to communicate with the Redis Tasks collection.
     * @param jmsCommunicator the jms communicator
     */
    @Autowired
    public TaskRouter(AgentsPool agentsPool, TasksRepository tasksRepository,
                      JmsCommunicator jmsCommunicator, TaskManager taskManager, RestRequest restRequest) {
        this.agentsPool = agentsPool;
        this.tasksRepository = tasksRepository;
        this.jmsCommunicator = jmsCommunicator;
        this.taskManager = taskManager;
        this.restRequest = restRequest;
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

        try {
            synchronized (precisionQueue.getServiceQueue()) {
                Task task = precisionQueue.peek();

                if (task == null) {
                    logger.debug("Queue [{}] is empty", this.precisionQueue.getName());
                    return;
                }

                logger.debug("Queue [{}] is not empty", this.precisionQueue.getName());

                if (task.isMarkedForDeletion()) {
                    precisionQueue.dequeue();
                    return;
                }

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
        if (task.getQueue().getId().equals(this.precisionQueue.getId())) {
            this.precisionQueue.enqueue(task);
            jmsCommunicator.publishTaskEnqueued(task, this.precisionQueue);
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
                Agent agent = this.getAvailableAgentWithLeastActiveTasks(step, task.getTopicId());
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

        String lastAssignedAgentId = task.getLastAssignedAgentId();
        logger.info("Last Assigned AgentId {} ", lastAssignedAgentId);
        if (lastAssignedAgentId != null) {
            Agent agent = this.agentsPool.findBy(lastAssignedAgentId);
            String mrdId = this.precisionQueue.getMrd().getId();
            if (agent != null && agent.isAvailableForRouting(mrdId, task.getTopicId())) {
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
    Agent getAvailableAgentWithLeastActiveTasks(Step step, String conversationId) {
        List<Agent> sortedAgentList = step.orderAgentsBy(AgentSelectionCriteria.LONGEST_AVAILABLE,
                this.precisionQueue.getMrd().getId());
        int lowestNumberOfTasks = Integer.MAX_VALUE;
        Agent result = null;
        for (Agent agent : sortedAgentList) {

            String mrdId = this.precisionQueue.getMrd().getId();
            int noOfTasksOnMrd = agent.getNoOfActiveQueueTasks(mrdId);

            if (agent.isAvailableForRouting(mrdId, conversationId) && noOfTasksOnMrd < lowestNumberOfTasks) {
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
            if (task.isMarkedForDeletion()) {
                logger.debug("AgentRequestTtlTimeout method returning..");
                return;
            }

            TaskState taskState = new TaskState(Enums.TaskStateName.RESERVED, null);
            boolean isPresented = this.presentTask(task, taskState, agent);

            if (isPresented) {
                agent.reserveTask(task);
                this.changeStateOf(task, taskState, agent);
                this.jmsCommunicator.publishTaskStateChangeForReporting(task);

                this.jmsCommunicator.publishAgentReserved(task, agent.toCcUser());

                task.getTimer().cancel();
                task.removePropertyChangeListener(Enums.EventName.STEP_TIMEOUT.name(), this);
                precisionQueue.dequeue();
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        logger.debug("method ended");
    }

    private boolean presentTask(Task task, TaskState taskState, Agent agent) {
        if (TaskUtility.getOfferToAgent(task)) {
            return this.restRequest.postAssignTask(task, agent.toCcUser(), taskState, false);
        }
        return true;
    }

    /**
     * Change state of.
     *
     * @param task    the task
     * @param state   the state
     * @param agent the agent
     */
    private void changeStateOf(Task task, TaskState state, Agent agent) {
        task.setTaskState(state);
        task.setAssignedTo(agent.toTaskAgent());
        this.tasksRepository.changeState(task.getId(), state);
        this.tasksRepository.updateAssignedTo(task.getId(), agent.toTaskAgent());
    }
}