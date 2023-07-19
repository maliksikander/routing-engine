package com.ef.mediaroutingengine.taskmanager;

import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Request ttl timers.
     */
    private final Map<String, TaskManager.RequestTtlTimer> requestTtlTimers;
    /**
     * The Change support.
     */
    private final PropertyChangeSupport changeSupport;
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;

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
                       TasksPool tasksPool, TasksRepository tasksRepository,
                       PrecisionQueuesPool precisionQueuesPool,
                       JmsCommunicator jmsCommunicator) {
        this.applicationContext = applicationContext;
        this.agentsPool = agentsPool;
        this.tasksPool = tasksPool;
        this.tasksRepository = tasksRepository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.requestTtlTimers = new ConcurrentHashMap<>();
        this.changeSupport = new PropertyChangeSupport(this);
        this.jmsCommunicator = jmsCommunicator;
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
        MediaRoutingDomain mediaRoutingDomain = task.getMrd();
        if (!mediaRoutingDomain.isManagedByRe()) {
            return;
        }
        String mrdId = task.getMrd().getId();
        Enums.AgentMrdStateName currentMrdState = agent.getAgentMrdState(mrdId).getState();
        int noOfTasks = agent.getNoOfActiveQueueTasks(mrdId);
        int maxAgentTasks = agent.getAgentMrdState(mrdId).getMaxAgentTasks();
        if (currentMrdState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY) && noOfTasks < 1) {
            this.agentMrdStateListener().propertyChange(agent, mrdId, Enums.AgentMrdStateName.NOT_READY, true);
        } else if (currentMrdState.equals(Enums.AgentMrdStateName.BUSY)) {
            if (noOfTasks == 0 && mediaRoutingDomain.isManagedByRe()) {
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
        MediaRoutingDomain mediaRoutingDomain = agent.getAgentMrdState(mrdId).getMrd();
        if (!mediaRoutingDomain.isManagedByRe()) {
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
        String topicId = channelSession.getConversationId();
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

    private void scheduleAgentRequestTimeoutTaskOnFailover(Task task) {
        String conversation = task.getTopicId();

        long ttlValue = getDelay(task.getChannelSession());
        long timeAlreadySpent = System.currentTimeMillis() - task.getEnqueueTime();

        long delay = ttlValue - timeAlreadySpent;

        // If a previous Agent request Ttl timer task exist cancel and remove it.
        this.cancelAgentRequestTtlTimerTask(conversation);
        this.removeAgentRequestTtlTimerTask(conversation);

        // Schedule a new timeout task
        Timer timer = new Timer();
        TaskManager.RequestTtlTimer newTimerTask = new TaskManager.RequestTtlTimer(conversation);
        timer.schedule(newTimerTask, delay);
        // Put the new task in the map.
        this.requestTtlTimers.put(conversation, newTimerTask);
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
        this.scheduleAgentRequestTimeoutTask(task.getChannelSession());
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
            this.scheduleAgentRequestTimeoutTaskOnFailover(task);

            PrecisionQueue queue = this.precisionQueuesPool.findById(task.getQueue().getId());
            if (queue == null) {
                logger.warn("Queue id: {} not found while enqueuing task", task.getQueue().getId());
                continue;
            }

            queue.enqueue(task);
            logger.debug("Task: {} enqueued in Precision-Queue: {}", task.getId(), queue.getId());
            task.addPropertyChangeListener(Enums.EventName.STEP_TIMEOUT.name(), queue.getTaskScheduler());
            task.setUpStepFrom(queue, 0);
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
            this.requestTtlTimers.remove(currentTask.getTopicId());
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
     * Cancels the Agent-Request-Ttl-Task for the topicId in the parameter if the timer is running.
     *
     * @param topicId timer task for this topicId is cancelled.
     */
    public void cancelAgentRequestTtlTimerTask(String topicId) {
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
     * Remove old task for reroute.
     *
     * @param task the task
     */
    public void removeFromPoolAndRepository(Task task) {
        this.tasksRepository.deleteById(task.getId());
        this.tasksPool.remove(task);
    }

    /**
     * Remove agent request ttl timer task.
     *
     * @param topicId the topic id
     */
    public void removeAgentRequestTtlTimerTask(String topicId) {
        this.requestTtlTimers.remove(topicId);
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

// +++++++++++++++++++++++++++++++ RequestTtlTimer class ++++++++++++++++++++++++++++++++++++++++++++

    /**
     * The type Request ttl timer.
     */
    private class RequestTtlTimer extends TimerTask {
        /**
         * The Topic id.
         */
        private final String conversation;

        /**
         * Instantiates a new Request ttl timer.
         *
         * @param conversation the topic id
         */
        public RequestTtlTimer(String conversation) {
            this.conversation = conversation;
        }

        public void run() {
            logger.info("Agent Request Ttl expired for request on conversation: {}", this.conversation);

            synchronized (TaskManager.this.tasksPool) {

                Task task = TaskManager.this.tasksPool.findInProcessTaskFor(this.conversation);

                if (task == null) {
                    logger.error("No In-Process Task found for this conversation, method returning...");
                    return;
                }

                PrecisionQueue queue = TaskManager.this.precisionQueuesPool.findById(task.getQueue().getId());
                synchronized (queue.getServiceQueue()) {
                    task.markForDeletion();
                }

                if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
                    logger.info("In process task: {} found in QUEUED state, removing task..", task.getId());

                    task.getTimer().cancel();
                    TaskManager.this.requestTtlTimers.remove(this.conversation);

                    // Remove task from precision-queue
                    queue.removeTask(task);

                    task.setTaskState(new TaskState(Enums.TaskStateName.CLOSED,
                            Enums.TaskStateReasonCode.NO_AGENT_AVAILABLE));

                    TaskManager.this.removeFromPoolAndRepository(task);
                    TaskManager.this.jmsCommunicator.publishTaskStateChangeForReporting(task);
                    TaskManager.this.jmsCommunicator.publishNoAgentAvailable(task);

                    logger.info("Queued task: {} removed successfully", task.getId());
                } else if (task.getTaskState().getName().equals(Enums.TaskStateName.RESERVED)) {
                    logger.info("In process task: {} found in Reserved state, task is marked for deletion",
                            task.getId());
                }
            }
        }
    }
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}
