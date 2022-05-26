package com.ef.mediaroutingengine.services.utilities;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.mediaroutingengine.commons.Constants;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Rest request.
     */
    private final RestRequest restRequest;
    /**
     * The Request ttl timers.
     */
    private final Map<UUID, TaskManager.RequestTtlTimer> requestTtlTimers;
    /**
     * The Change support.
     */
    private final PropertyChangeSupport changeSupport;

    /**
     * Default Constructor. Loads the dependencies.
     *
     * @param agentsPool          pool of all agents.
     * @param applicationContext  to get beans at runtime.
     * @param tasksPool           the tasks pool
     * @param tasksRepository     the tasks repository
     * @param precisionQueuesPool the precision queues pool
     * @param restRequest         the rest request
     */
    @Autowired
    public TaskManager(AgentsPool agentsPool, ApplicationContext applicationContext,
                       TasksPool tasksPool, TasksRepository tasksRepository,
                       PrecisionQueuesPool precisionQueuesPool, RestRequest restRequest) {
        this.applicationContext = applicationContext;
        this.agentsPool = agentsPool;
        this.tasksPool = tasksPool;
        this.tasksRepository = tasksRepository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.restRequest = restRequest;
        this.requestTtlTimers = new ConcurrentHashMap<>();
        this.changeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Removes a task from the agent it is associated to. Changes the agent's MRD state wrt to number of
     * tasks left after the task is removed.
     *
     * @param task task to remove.
     */
    public void endTaskFromAssignedAgent(Task task) {
        RoutingMode routingMode = task.getRoutingMode();
        Agent agent = this.agentsPool.findById(task.getAssignedTo());

        if (agent == null || routingMode == null) {
            return;
        }

        if (routingMode.equals(RoutingMode.PUSH)) {
            this.endPushTaskFromAssignedAgent(task, agent);
        } else if (routingMode.equals(RoutingMode.PULL)) {
            this.endPullTaskFromAssignedAgent(task, agent);
        }
    }

    private void endPushTaskFromAssignedAgent(Task task, Agent agent) {
        agent.removeTask(task);

        String mrdId = task.getMrd().getId();
        Enums.AgentMrdStateName currentMrdState = agent.getAgentMrdState(mrdId).getState();
        int noOfTasks = agent.getNoOfActivePushTasks(mrdId);
        int maxTasks = agent.getAgentMrdState(mrdId).getMaxTask();

        if (currentMrdState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY) && noOfTasks < 1) {
            this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.NOT_READY, true);
        } else if (currentMrdState.equals(Enums.AgentMrdStateName.BUSY)) {
            if (noOfTasks == 0) {
                this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.READY, true);
//            } else if (noOfTasks < task.getMrd().getMaxRequests()) {
            } else if (noOfTasks < maxTasks) {
                this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.ACTIVE, true);
            }
        } else if (currentMrdState.equals(Enums.AgentMrdStateName.ACTIVE)) {
//            if (noOfTasks >= task.getMrd().getMaxRequests()) {
            if (noOfTasks >= maxTasks) {
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
        Agent agent = this.agentsPool.findById(task.getAssignedTo());

        if (agent != null) {
            agent.removeReservedTask();
            AgentState agentState = new AgentState(Enums.AgentStateName.NOT_READY, null);
            this.agentStateListener().propertyChange(agent, agentState);
        }
    }

    /**
     * Updates the Agent's MRD state, when task state changes to active.
     *
     * @param agent agent to b updated
     * @param mrdId the mrd id
     */
    public void updateAgentMrdState(Agent agent, String mrdId) {
        int noOfActiveTasks = agent.getNoOfActivePushTasks(mrdId);
//        int maxRequestAllowed = agent.getAgentMrdState(mrdId).getMrd().getMaxRequests();
        int maxRequestAllowed = agent.getAgentMrdState(mrdId).getMaxTask();

        if (noOfActiveTasks >= maxRequestAllowed) {
            this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.BUSY, false);
        } else if (noOfActiveTasks == 1) {
            this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.ACTIVE, false);
        }

        if (noOfActiveTasks > 1) {
            for (PrecisionQueue precisionQueue : this.precisionQueuesPool.toList()) {
                if (precisionQueue.getMrd().getId().equals(mrdId)) {
                    PropertyChangeEvent evt = new PropertyChangeEvent(this,
                            Enums.EventName.TASK_ACCEPTED.name(), null, "");
                    precisionQueue.getTaskScheduler().propertyChange(evt);
                }
            }
        }
    }

    /**
     * Gets delay.
     *
     * @param channelSession the channel session
     * @return the delay
     */
    private long getDelay(ChannelSession channelSession) {
        int ttl = channelSession.getChannel().getChannelConfig().getRoutingPolicy().getAgentRequestTtl();
        return ttl * 1000L;
    }

    /**
     * Schedule agent request timeout task.
     *
     * @param channelSession the channel session
     */
    private void scheduleAgentRequestTimeoutTask(ChannelSession channelSession) {
        UUID topicId = channelSession.getTopicId();
        long delay = getDelay(channelSession);
        // If a previous Agent request Ttl timer task exist cancel and remove it.
        this.cancelAgentRequestTtlTimerTask(topicId);
        this.removeAgentRequestTtlTimerTask(topicId);

        // Schedule a new timeout task
        Timer timer = new Timer();
        TaskManager.RequestTtlTimer newTimerTask = new TaskManager.RequestTtlTimer(topicId);
        timer.schedule(newTimerTask, delay);
        // Put the new task in the map.
        this.requestTtlTimers.put(topicId, newTimerTask);
    }

    private void insertInPoolAndRepository(Task task) {
        this.tasksPool.add(task);
        logger.debug("Task: {} added in tasks pool", task.getId());

        this.tasksRepository.save(task.getId().toString(), new TaskDto(task));
        logger.debug("Task: {} saved in tasks repository", task.getId());
    }

    /**
     * Enqueue task from assign-resource API call.
     *
     * @param channelSession channel session in request.
     * @param queue          queue in request.
     * @param mrd            mrd in request.
     */
    public void enqueueTask(ChannelSession channelSession, PrecisionQueue queue, MediaRoutingDomain mrd) {
        logger.debug(Constants.METHOD_STARTED);

        TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        Task task = Task.getInstanceFrom(channelSession, mrd, queue.getId(), taskState);

        this.insertInPoolAndRepository(task);
        this.publishTaskForReporting(task);

        this.scheduleAgentRequestTimeoutTask(task.getChannelSession());
        logger.debug("Agent-Request-Ttl timer task scheduled");

        this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, task);
        logger.debug(Constants.METHOD_ENDED);
    }

    /**
     * Enqueues task all present in the redis DB at start of application.
     *
     * @param task task to be enqueued.
     */
    public void enqueueTaskOnFailover(Task task) {
        PrecisionQueue queue = this.precisionQueuesPool.findById(task.getQueue());
        if (queue != null) {
            this.tasksPool.add(task);
            if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
                this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, task);
            }
        } else {
            logger.warn("Queue id: {} not found while enqueuing task", task.getQueue());
        }
    }

    /**
     * Reroute reserved task.
     *
     * @param currentTask the current task
     */
    public void rerouteReservedTask(Task currentTask) {
        this.removeFromPoolAndRepository(currentTask);
        // If Agent request Ttl has ended.
        if (currentTask.isMarkedForDeletion()) {
            this.requestTtlTimers.remove(currentTask.getTopicId());
            this.restRequest.postNoAgentAvailable(currentTask.getTopicId().toString());
            this.publishTaskForReporting(currentTask);
            return;
        }

        Task newTask = Task.getInstanceFrom(currentTask);
        this.insertInPoolAndRepository(newTask);
        this.publishTaskForReporting(newTask);
        this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, newTask);
    }

    /**
     * Cancels the Agent-Request-Ttl-Task for the topicId in the parameter if the timer is running.
     *
     * @param topicId timer task for this topicId is cancelled.
     */
    public void cancelAgentRequestTtlTimerTask(UUID topicId) {
        TaskManager.RequestTtlTimer requestTtlTimer = this.requestTtlTimers.get(topicId);
        if (requestTtlTimer == null) {
            return;
        }
        try {
            requestTtlTimer.cancel();
        } catch (IllegalStateException e) {
            logger.warn("Agent Request Ttl timer on topic: {} is already cancelled", topicId);
        }
    }

    /**
     * Removes the task from the pool by the task id.
     *
     * @param task the task to be removed
     */
    public void removeTask(Task task) {
        logger.debug("Going to remove task: {}", task.getId());
        if (task.getRoutingMode().equals(RoutingMode.PUSH)) {
            this.cancelAgentRequestTtlTimerTask(task.getTopicId());
            this.requestTtlTimers.remove(task.getTopicId());
        }
        this.tasksPool.remove(task);
    }

    /**
     * Remove old task for reroute.
     *
     * @param task the task
     */
    public void removeFromPoolAndRepository(Task task) {
        this.tasksRepository.deleteById(task.getId().toString());
        this.tasksPool.remove(task);
    }

    /**
     * Remove agent request ttl timer task.
     *
     * @param topicId the topic id
     */
    public void removeAgentRequestTtlTimerTask(UUID topicId) {
        this.requestTtlTimers.remove(topicId);
    }

    /**
     * End pull task on agent logout.
     *
     * @param task the task
     */
    public void removeTaskOnAgentLogout(Task task) {
        task.setTaskState(new TaskState(Enums.TaskStateName.CLOSED, null));
        this.removeFromPoolAndRepository(task);
        this.publishTaskForReporting(task);
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

    public void publishTaskForReporting(Task task) {
        this.jmsCommunicator().publishTaskStateChangeForReporting(task);
    }

    private JmsCommunicator jmsCommunicator() {
        return this.applicationContext.getBean(JmsCommunicator.class);
    }

    private AgentStateListener agentStateListener() {
        return this.applicationContext.getBean(AgentStateListener.class);
    }

    private AgentMrdStateListener agentMrdStateListener() {
        return this.applicationContext.getBean(AgentMrdStateListener.class);
    }

// +++++++++++++++++++++++++++++++ RequestTtlTimer class ++++++++++++++++++++++++++++++++++++++++++++

    /**
     * The type Request ttl timer.
     */
    private class RequestTtlTimer extends TimerTask {
        /**
         * The Topic id.
         */
        private final UUID topicId;

        /**
         * Instantiates a new Request ttl timer.
         *
         * @param topicId the topic id
         */
        public RequestTtlTimer(UUID topicId) {
            this.topicId = topicId;
        }

        public void run() {
            logger.debug(Constants.METHOD_STARTED);
            Task task = TaskManager.this.tasksPool.findFirstByConversationId(topicId);
            if (task == null) {
                logger.error("Task not found in task pool | AgentRequestTtl Timer run method returning...");
                return;
            }

            task.markForDeletion(Enums.TaskStateReasonCode.NO_AGENT_AVAILABLE);
            if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
                task.getTimer().cancel();
                // Remove task from precision-queue
                PrecisionQueue queue = TaskManager.this.precisionQueuesPool.findById(task.getQueue());
                if (queue != null) {
                    queue.removeTask(task);
                }
                // Remove task from redis.
                TaskManager.this.tasksRepository.deleteById(task.getId().toString());
                // Remove task from task pool
                TaskManager.this.tasksPool.remove(task);
                TaskManager.this.requestTtlTimers.remove(this.topicId);
                // post no agent available
                TaskManager.this.restRequest.postNoAgentAvailable(this.topicId.toString());
                //publish task for reporting
                TaskManager.this.publishTaskForReporting(task);
                logger.debug("Agent request TTL expired. Queued task: {} removed", task.getId());
            }
            logger.debug(Constants.METHOD_ENDED);
        }
    }
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}
