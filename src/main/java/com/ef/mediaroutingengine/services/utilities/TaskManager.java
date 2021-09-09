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
import java.util.LinkedList;
import java.util.List;
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
     * The Change support.
     */
    private final PropertyChangeSupport changeSupport;
    /**
     * The Request ttl timers.
     */
    private final Map<UUID, TaskManager.RequestTtlTimer> requestTtlTimers;
    /**
     * The Change support precision queue listeners.
     */
    private final List<String> changeSupportListeners = new LinkedList<>();

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
        this.changeSupport = new PropertyChangeSupport(this);
        this.requestTtlTimers = new ConcurrentHashMap<>();
    }

    /**
     * Adds a property change listener, which will listen to property changes of this object.
     *
     * @param listener the property change listener object
     * @param name     the name of the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener, String name) {
        if (!this.changeSupportListeners.contains(name)) {
            this.changeSupportListeners.add(name);
            this.changeSupport.addPropertyChangeListener(listener);
        }
    }

    /**
     * Remove property change listener.
     *
     * @param listener the listener
     * @param name     the name
     */
    public void removePropertyChangeListener(PropertyChangeListener listener, String name) {
        this.changeSupportListeners.remove(name);
        this.changeSupport.removePropertyChangeListener(listener);
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

        if (currentMrdState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY) && noOfTasks < 1) {
            this.fireAgentMrdChangeRequest(agent, mrdId, Enums.AgentMrdStateName.NOT_READY, true);
        } else if (currentMrdState.equals(Enums.AgentMrdStateName.BUSY)) {
            this.fireAgentMrdChangeRequest(agent, mrdId, Enums.AgentMrdStateName.ACTIVE, true);
        } else if (currentMrdState.equals(Enums.AgentMrdStateName.ACTIVE) && noOfTasks < 1) {
            this.fireAgentMrdChangeRequest(agent, mrdId, Enums.AgentMrdStateName.READY, true);
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
            this.fireAgentStateChangeRequest(agent, agentState);
        }
    }

    /**
     * Fire agent mrd change request.
     *
     * @param agent the agent
     * @param mrdId the mrd id
     * @param state the state
     * @param async the async
     */
    private void fireAgentMrdChangeRequest(Agent agent, String mrdId, Enums.AgentMrdStateName state, boolean async) {
        AgentMrdStateListener listener = this.applicationContext.getBean(AgentMrdStateListener.class);
        listener.propertyChange(agent, mrdId, state, async);
    }

    /**
     * Fire agent state change request.
     *
     * @param agent      the agent
     * @param agentState the agent state
     */
    private void fireAgentStateChangeRequest(Agent agent, AgentState agentState) {
        AgentStateListener listener = this.applicationContext.getBean(AgentStateListener.class);
        listener.propertyChange(agent, agentState);
    }

    /**
     * Updates the Agent's MRD state, when task state changes to active.
     *
     * @param agent agent to b updated
     * @param mrdId the mrd id
     */
    public void updateAgentMrdState(Agent agent, String mrdId) {
        int noOfActiveTasks = agent.getNoOfActivePushTasks(mrdId);

        if (noOfActiveTasks == 1) {
            this.fireAgentMrdChangeRequest(agent, mrdId, Enums.AgentMrdStateName.ACTIVE, false);
        } else if (noOfActiveTasks == Constants.MAX_TASKS) {
            this.fireAgentMrdChangeRequest(agent, mrdId, Enums.AgentMrdStateName.BUSY, false);
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

    /**
     * Enqueue task from assign-resource API call.
     *
     * @param channelSession channel session in request.
     * @param queue          queue in request.
     * @param mrd            mrd in request.
     */
    public void enqueueTask(ChannelSession channelSession, PrecisionQueue queue, MediaRoutingDomain mrd) {
        logger.debug("method started | TasksPool.enqueueTask method");
        logger.debug("Precision-Queue is not null | TasksPool.enqueueTask method");

        TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        Task task = new Task(channelSession, mrd, queue, taskState);
        this.tasksPool.add(task);
        logger.debug("New task added to allTasks list | TasksPool.enqueueTask method");

        this.tasksRepository.save(task.getId().toString(), new TaskDto(task));
        logger.debug("Task saved in Redis | TasksPool.enqueueTask method");

        task.setStep(queue.getStepAt(0));
        queue.enqueue(task);
        logger.debug("Task enqueued in Precision-Queue | TasksPool.enqueueTask method");
        task.setTimeouts(queue.getTimeouts());

        JmsCommunicator jmsCommunicator = this.applicationContext.getBean(JmsCommunicator.class);
        jmsCommunicator.publishTaskStateChangeForReporting(task);

        this.scheduleAgentRequestTimeoutTask(task.getChannelSession());
        logger.debug("Agent-Request-Ttl task scheduled | TasksPool.enqueueTask method");

        this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, task);
        logger.debug("NEW_TASK event fired to Task-Scheduler | TasksPool.enqueueTask method");
        logger.debug("method ended | TasksPool.enqueueTask method");
    }

    /**
     * Enqueues task all present in the redis DB at start of application.
     *
     * @param task task to be enqueued.
     */
    public void enqueueTask(Task task) {
        PrecisionQueue queue = this.precisionQueuesPool.findById(task.getQueue().getId());
        if (queue != null) {
            this.tasksPool.add(task);
            if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
                queue.enqueue(task);
                task.setTimeouts(queue.getTimeouts());
                this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, task);
            }
        } else {
            logger.warn("Queue id: {} not found while enqueuing task", task.getQueue());
        }
    }

    /**
     * Remove old task for reroute.
     *
     * @param task the task
     */
    public void removeTaskFromPoolAndRepository(Task task) {
        this.tasksRepository.deleteById(task.getId().toString());
        this.tasksPool.remove(task);
    }

    /**
     * Sets up new task for reroute.
     *
     * @param oldTask the old task
     * @return the up new task for reroute
     */
    private Task setUpNewTaskForReroute(Task oldTask) {
        Task newTask = new Task(oldTask);
        this.tasksPool.add(newTask);
        this.tasksRepository.save(newTask.getId().toString(), new TaskDto(newTask));

        PrecisionQueue queue = this.precisionQueuesPool.findById(newTask.getQueue().getId());
        queue.enqueue(newTask);
        newTask.setEnqueueTime(System.currentTimeMillis());
        newTask.setTimeouts(queue.getTimeouts());
        return newTask;
    }

    /**
     * Reroute active task.
     *
     * @param currentTask the current task
     */
    private void rerouteActiveTask(Task currentTask) {
        this.removeTaskFromPoolAndRepository(currentTask);
        Task newTask = this.setUpNewTaskForReroute(currentTask);
        JmsCommunicator jmsCommunicator = this.applicationContext.getBean(JmsCommunicator.class);
        jmsCommunicator.publishTaskStateChangeForReporting(newTask);
        this.scheduleAgentRequestTimeoutTask(newTask.getChannelSession());
        this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, newTask);
    }

    /**
     * Reroute reserved task.
     *
     * @param currentTask the current task
     */
    private void rerouteReservedTask(Task currentTask) {
        this.removeTaskFromPoolAndRepository(currentTask);
        // If Agent request Ttl has ended.
        if (currentTask.isAgentRequestTimeout()) {
            this.requestTtlTimers.remove(currentTask.getTopicId());
            this.restRequest.postNoAgentAvailable(currentTask.getTopicId().toString());
            return;
        }
        Task newTask = this.setUpNewTaskForReroute(currentTask);
        JmsCommunicator jmsCommunicator = this.applicationContext.getBean(JmsCommunicator.class);
        jmsCommunicator.publishTaskStateChangeForReporting(newTask);
        this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, newTask);
    }

    /**
     * Reroutes a task to be assigned to another agent. Deletes the task in request, creates a new task from
     * the task in request and enqueue the newly created task.
     *
     * @param task Task to reschedule.
     */
    public void rerouteTask(Task task) {
        if (task.getTaskState().getName().equals(Enums.TaskStateName.RESERVED)) {
            this.rerouteReservedTask(task);
        } else if (task.getTaskState().getName().equals(Enums.TaskStateName.ACTIVE)) {
            this.rerouteActiveTask(task);
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
    public void removePullTaskOnAgentLogout(Task task) {
        task.setTaskState(new TaskState(Enums.TaskStateName.CLOSED, null));
        this.removeTaskFromPoolAndRepository(task);
        JmsCommunicator jmsCommunicator = this.applicationContext.getBean(JmsCommunicator.class);
        jmsCommunicator.publishTaskStateChangeForReporting(task);
    }

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
            logger.debug("method started | RequestTtlTimer.run method");
            Task task = TaskManager.this.tasksPool.findByConversationId(topicId);
            if (task == null) {
                logger.error("Task not found in task pool | AgentRequestTtl Timer run method returning...");
                return;
            }

            task.agentRequestTimeout();
            if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
                // Remove task from precision-queue
                PrecisionQueue queue = TaskManager.this.precisionQueuesPool.findById(task.getQueue().getId());
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
            }
            logger.debug("method ended | RequestTtlTimer.run method");
        }
    }
}
