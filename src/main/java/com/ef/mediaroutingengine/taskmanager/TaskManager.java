package com.ef.mediaroutingengine.taskmanager;

import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.MrdType;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.AgentRequestTimerService;
import com.ef.mediaroutingengine.routing.StepTimerService;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdTypePool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;


/**
 * The type Task manager.
 */
@Service
public class TaskManager {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
    /**
     * The Application context.
     */
    private final ApplicationContext applicationContext;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    /**
     * The Mrd type pool.
     */
    private final MrdTypePool mrdTypePool;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Change support.
     */
    private final PropertyChangeSupport changeSupport;
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    private final StepTimerService stepTimerService;
    private final AgentRequestTimerService agentRequestTimerService;

    /**
     * Default Constructor. Loads the dependencies.
     *
     * @param agentsPool          pool of all agents.
     * @param applicationContext  to get beans at runtime.
     * @param tasksPool           the tasks pool
     * @param tasksRepository     the tasks repository
     * @param precisionQueuesPool the precision queues pool
     */
    @Autowired
    public TaskManager(AgentsPool agentsPool, ApplicationContext applicationContext,
                       TasksPool tasksPool, MrdTypePool mrdTypePool, TasksRepository tasksRepository,
                       PrecisionQueuesPool precisionQueuesPool,
                       JmsCommunicator jmsCommunicator, StepTimerService stepTimerService,
                       AgentRequestTimerService agentRequestTimerService) {
        this.applicationContext = applicationContext;
        this.agentsPool = agentsPool;
        this.tasksPool = tasksPool;
        this.mrdTypePool = mrdTypePool;
        this.tasksRepository = tasksRepository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.changeSupport = new PropertyChangeSupport(this);
        this.jmsCommunicator = jmsCommunicator;
        this.stepTimerService = stepTimerService;
        this.agentRequestTimerService = agentRequestTimerService;
    }

    /**
     * Removes a task from the agent it is associated to. Changes the agent's MRD state wrt to number of
     * tasks left after the task is removed.
     *
     * @param task task to remove.
     */
    public void endTaskFromAssignedAgent(Task task) {
        Agent agent = this.agentsPool.findBy(task.getAssignedTo());

        if (agent == null) {
            return;
        }

        if (task.getType().getMode().equals(Enums.TaskTypeMode.QUEUE)) {
            // If a reserved task is closed remove the reserve task from agent.
            if (agent.getReservedTask() != null && agent.getReservedTask().getId().equals(task.getId())) {
                agent.removeReservedTask();
                return;
            }
            // If an active task is closed, remove the active task
            this.endPushTaskFromAssignedAgent(task, agent);
            return;
        }

        this.endPullTaskFromAssignedAgent(task, agent);
    }

    private void endPushTaskFromAssignedAgent(Task task, Agent agent) {
        agent.removeTask(task);
        MrdType mrdType = this.mrdTypePool.getById(task.getMrd().getType());

        if (!mrdType.isManagedByRe()) {
            return;
        }
        String mrdId = task.getMrd().getId();
        Enums.AgentMrdStateName currentMrdState = agent.getAgentMrdState(mrdId).getState();
        int noOfTasks = agent.getNoOfActiveQueueTasks(mrdId);
        int maxAgentTasks = agent.getAgentMrdState(mrdId).getMaxAgentTasks();
        if (currentMrdState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY) && noOfTasks < 1) {
            this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.NOT_READY, true);
        } else if (currentMrdState.equals(Enums.AgentMrdStateName.BUSY)) {
            if (noOfTasks == 0 && mrdType.isManagedByRe()) {
                this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.READY, true);
            } else if (noOfTasks < maxAgentTasks) {
                this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.ACTIVE, true);
            }
        } else if (currentMrdState.equals(Enums.AgentMrdStateName.ACTIVE)) {
            if (noOfTasks >= maxAgentTasks) {
                this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.BUSY, true);
            } else if (noOfTasks < 1) {
                this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.READY, true);
            }
        }
    }

    private void endPullTaskFromAssignedAgent(Task task, Agent agent) {
        agent.removeTask(task);
    }

    /**
     * Removes the task from the associated agent when a task is closed with reasonCode Rona. Requests to change
     * the agent's mrd state to not-ready.
     *
     * @param task task to be removed.
     */
    public void endTaskFromAgentOnRona(Task task) {
        Agent agent = this.agentsPool.findBy(task.getAssignedTo());

        if (agent != null) {
            agent.removeReservedTask();
            AgentState agentState = new AgentState(Enums.AgentStateName.NOT_READY, null);
            this.agentStateListener().propertyChange(agent, agentState, true);
        }
    }

    /**
     * Updates the Agent's MRD state, when task state changes to active.
     *
     * @param agent agent to b updated
     * @param mrdId the mrd id
     */
    public void updateAgentMrdState(Agent agent, String mrdId) {
        MrdType mrdType = this.mrdTypePool.getById(agent.getAgentMrdState(mrdId).getMrd().getType());
        if (!mrdType.isManagedByRe()) {
            return;
        }

        int noOfActiveTasks = agent.getNoOfActiveQueueTasks(mrdId);
        int maxRequestAllowed = agent.getAgentMrdState(mrdId).getMaxAgentTasks();
        if (noOfActiveTasks >= maxRequestAllowed) {
            this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.BUSY, false);
        } else if (noOfActiveTasks == 1) {
            this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.ACTIVE, false);
        }
        if (noOfActiveTasks > 1) {
            String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);

            CompletableFuture.runAsync(() -> {
                // putting same correlation id from the caller thread into this thread
                MDC.put(Constants.MDC_CORRELATION_ID, correlationId);

                for (PrecisionQueue precisionQueue : this.precisionQueuesPool.toList()) {
                    if (precisionQueue.getMrd().getId().equals(mrdId)) {
                        PropertyChangeEvent evt = new PropertyChangeEvent(this,
                                Enums.EventName.TASK_ACCEPTED.name(), null, "");
                        precisionQueue.getTaskScheduler().propertyChange(evt);
                    }
                }

                MDC.clear();
            });
        }
    }

    /**
     * Insert in pool and repository.
     *
     * @param task the task
     */
    public void insertInPoolAndRepository(Task task) {
        this.tasksPool.add(task);
        logger.debug("Task: {} added in tasks pool", task.getId());

        this.tasksRepository.save(task.getId(), AdapterUtility.createTaskDtoFrom(task));
        logger.debug("Task: {} saved in tasks repository", task.getId());
    }

    /**
     * Enqueue task from assign-resource API call.
     *
     * @param channelSession channel session in request.
     * @param queue          queue in request.
     * @param mrd            mrd in request.
     */
    public void enqueueTask(ChannelSession channelSession, PrecisionQueue queue, MediaRoutingDomain mrd,
                            TaskType requestType, int priority) {
        logger.debug(Constants.METHOD_STARTED);

        TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        Task task = Task.getInstanceFrom(channelSession, mrd, queue.toTaskQueue(), taskState, requestType, priority);

        this.insertInPoolAndRepository(task);
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);

        this.agentRequestTimerService.start(task, queue);
        logger.debug("Agent-Request-Ttl timer task scheduled");

        this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, task);

        logger.debug(Constants.METHOD_ENDED);
    }

    /**
     * Enqueues task all present in the redis DB at start of application.
     */
    public void enqueueQueuedTasksOnFailover() {
        List<Task> queuedTasks = this.tasksPool.findAllQueuedTasks();

        for (Task task : queuedTasks) {

            PrecisionQueue queue = this.precisionQueuesPool.findById(task.getQueue().getId());
            if (queue == null) {
                logger.warn("Queue id: {} not found while enqueuing task", task.getQueue().getId());
                continue;
            }

            this.agentRequestTimerService.startOnFailover(task, queue);

            queue.enqueue(task);
            logger.debug("Task: {} enqueued in Precision-Queue: {}", task.getId(), queue.getId());

            this.stepTimerService.startNext(task, queue, 0);
        }

        this.changeSupport.firePropertyChange("FIRE_ON_FAILOVER", null, null);
    }

    /**
     * Reroute reserved task.
     *
     * @param currentTask the current task
     */
    public void rerouteReservedTask(Task currentTask) {
        // If Agent request Ttl has ended.
        if (currentTask.isMarkedForDeletion()) {
            this.agentRequestTimerService.stop(currentTask.getTopicId());
            this.jmsCommunicator.publishNoAgentAvailable(currentTask);
            return;
        }

        Task newTask = Task.getInstanceFrom(currentTask);
        this.insertInPoolAndRepository(newTask);
        this.jmsCommunicator.publishTaskStateChangeForReporting(newTask);

        String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);
        CompletableFuture.runAsync(() -> {
            MDC.put(Constants.MDC_CORRELATION_ID, correlationId);
            this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, newTask);
            MDC.clear();
        });
    }

    /**
     * Remove old task for reroute.
     *
     * @param task the task
     */
    public void removeFromPoolAndRepository(Task task) {
        this.tasksRepository.deleteById(task.getId());
        this.tasksPool.remove(task);
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
     * Remove property change listener.
     *
     * @param property the property
     * @param listener the listener
     */
    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(property, listener);
    }

    private AgentStateListener agentStateListener() {
        return this.applicationContext.getBean(AgentStateListener.class);
    }

    private AgentMrdStateListener agentMrdStateListener() {
        return this.applicationContext.getBean(AgentMrdStateListener.class);
    }
}
