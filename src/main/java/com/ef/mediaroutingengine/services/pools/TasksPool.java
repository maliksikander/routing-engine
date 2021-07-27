package com.ef.mediaroutingengine.services.pools;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.utilities.RestRequest;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
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
import org.springframework.stereotype.Service;

/**
 * The type Tasks pool.
 */
@Service
public class TasksPool {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TasksPool.class);

    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Rest request.
     */
    private final RestRequest restRequest;
    /**
     * The All tasks.
     */
    private final List<Task> allTasks;
    /**
     * The Request ttl timers.
     */
    private final Map<UUID, RequestTtlTimer> requestTtlTimers;

    /**
     * The Change support.
     */
    private final PropertyChangeSupport changeSupport;
    /**
     * The Change support precision queue listeners.
     */
    private final List<String> changeSupportPrecisionQueueListeners = new LinkedList<>();

    /**
     * Default constructor. Autowired -> loads Dependencies.
     *
     * @param precisionQueuesPool pool of all precision queues.
     * @param tasksRepository     the tasks repository
     * @param restRequest         the rest request
     */
    @Autowired
    public TasksPool(PrecisionQueuesPool precisionQueuesPool, TasksRepository tasksRepository,
                     RestRequest restRequest) {
        this.precisionQueuesPool = precisionQueuesPool;
        this.tasksRepository = tasksRepository;
        this.restRequest = restRequest;
        this.allTasks = new LinkedList<>();
        this.requestTtlTimers = new ConcurrentHashMap<>();
        this.changeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Adds a property change listener, which will listen to property changes of this object.
     *
     * @param listener the property change listener object
     * @param name     the name of the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener, String name) {
        if (!this.changeSupportPrecisionQueueListeners.contains(name)) {
            this.changeSupportPrecisionQueueListeners.add(name);
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
        this.changeSupportPrecisionQueueListeners.remove(name);
        this.changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Contains boolean.
     *
     * @param task the task
     * @return the boolean
     */
    private boolean contains(Task task) {
        for (Task element : this.allTasks) {
            if (element.getId().equals(task.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add.
     *
     * @param task the task
     */
    private void add(Task task) {
        if (!this.contains(task)) {
            this.allTasks.add(task);
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
        RequestTtlTimer newTimerTask = new RequestTtlTimer(topicId);
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
        LOGGER.debug("method started | TasksPool.enqueueTask method");
        LOGGER.debug("Precision-Queue is not null | TasksPool.enqueueTask method");
        Task task = new Task(channelSession, mrd, queue.getId());
        this.add(task);
        LOGGER.debug("New task added to allTasks list | TasksPool.enqueueTask method");
        this.tasksRepository.save(task.getId().toString(), new TaskDto(task));
        LOGGER.debug("Task saved in Redis | TasksPool.enqueueTask method");
        queue.enqueue(task);
        LOGGER.debug("Task enqueued in Precision-Queue | TasksPool.enqueueTask method");
        task.setTimeouts(queue.getTimeouts());
        this.scheduleAgentRequestTimeoutTask(task.getChannelSession());
        LOGGER.debug("Agent-Request-Ttl task scheduled | TasksPool.enqueueTask method");
        this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, task);
        LOGGER.debug("NEW_TASK event fired to Task-Scheduler | TasksPool.enqueueTask method");
        LOGGER.debug("method ended | TasksPool.enqueueTask method");
    }

    /**
     * Enqueues task all present in the redis DB at start of application.
     *
     * @param task task to be enqueued.
     */
    public void enqueueTask(Task task) {
        PrecisionQueue queue = this.precisionQueuesPool.findById(task.getQueue());
        if (queue != null) {
            this.add(task);
            if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
                queue.enqueue(task);
                task.setTimeouts(queue.getTimeouts());
                this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, task);
            }
        } else {
            LOGGER.warn("Queue id: {} not found while enqueuing task", task.getQueue());
        }
    }

    /**
     * Remove old task for reroute.
     *
     * @param task the task
     */
    private void removeOldTaskForReroute(Task task) {
        this.tasksRepository.deleteById(task.getId().toString());
        this.allTasks.remove(task);
    }

    /**
     * Sets up new task for reroute.
     *
     * @param oldTask the old task
     * @return the up new task for reroute
     */
    private Task setUpNewTaskForReroute(Task oldTask) {
        Task newTask = new Task(oldTask);
        this.allTasks.add(newTask);
        this.tasksRepository.save(newTask.getId().toString(), new TaskDto(newTask));

        PrecisionQueue queue = this.precisionQueuesPool.findById(newTask.getQueue());
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
        this.removeOldTaskForReroute(currentTask);
        Task newTask = this.setUpNewTaskForReroute(currentTask);
        this.scheduleAgentRequestTimeoutTask(newTask.getChannelSession());
        this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, newTask);
    }

    /**
     * Reroute reserved task.
     *
     * @param currentTask the current task
     */
    private void rerouteReservedTask(Task currentTask) {
        this.removeOldTaskForReroute(currentTask);
        // If Agent request Ttl has ended.
        if (currentTask.isAgentRequestTimeout()) {
            this.requestTtlTimers.remove(currentTask.getTopicId());
            this.restRequest.postNoAgentAvailable(currentTask.getTopicId().toString());
            return;
        }
        Task newTask = this.setUpNewTaskForReroute(currentTask);
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
     * Find all list.
     *
     * @return the list
     */
    public List<Task> findAll() {
        return this.allTasks;
    }

    /**
     * Returns task by task-id from the tasks pool.
     *
     * @param taskId id of the task to find
     * @return TaskService object if found, null otherwise
     */
    public Task findById(UUID taskId) {
        for (Task task : this.allTasks) {
            if (task.getId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Returns task by conversationId from the task pool.
     *
     * @param conversationId the conversation-id to search task by
     * @return task if found, null otherwise
     */
    public Task findByConversationId(UUID conversationId) {
        for (Task task : this.allTasks) {
            if (task.getTopicId().equals(conversationId)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Removes the task from the pool by the task id.
     *
     * @param task the task to be removed
     * @return true if found and removed, false otherwise
     */
    public boolean removeTask(Task task) {
        LOGGER.debug("Going to remove task: {}", task.getId());
        this.cancelAgentRequestTtlTimerTask(task.getTopicId());
        this.requestTtlTimers.remove(task.getTopicId());
        return this.allTasks.remove(task);
    }

    /**
     * Cancels the Agent-Request-Ttl-Task for the topicId in the parameter if the timer is running.
     *
     * @param topicId timer task for this topicId is cancelled.
     */
    public void cancelAgentRequestTtlTimerTask(UUID topicId) {
        RequestTtlTimer requestTtlTimer = this.requestTtlTimers.get(topicId);
        if (requestTtlTimer == null) {
            return;
        }
        try {
            requestTtlTimer.cancel();
        } catch (IllegalStateException e) {
            LOGGER.warn("Agent Request Ttl timer on topic: {} is already cancelled", topicId);
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
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return this.allTasks.size();
    }

    /**
     * Returns all Active Agent-Request-Ttl timers.
     *
     * @return list of Active Agent-Request-Ttl timers.
     */
    public List<UUID> getAllActiveTimers() {
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, RequestTtlTimer> entry : this.requestTtlTimers.entrySet()) {
            result.add(entry.getValue().topicId);
        }
        return result;
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
            LOGGER.debug("method started | RequestTtlTimer.run method");
            Task task = TasksPool.this.findByConversationId(topicId);
            if (task == null) {
                LOGGER.error("Task not found in task pool | AgentRequestTtl Timer run method returning...");
                return;
            }

            task.agentRequestTimeout();
            if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
                // Remove task from precision-queue
                PrecisionQueue queue = TasksPool.this.precisionQueuesPool.findById(task.getQueue());
                if (queue != null) {
                    queue.removeTask(task);
                }
                // Remove task from redis.
                TasksPool.this.tasksRepository.deleteById(task.getId().toString());
                // Remove task from task pool
                TasksPool.this.allTasks.remove(task);
                TasksPool.this.requestTtlTimers.remove(this.topicId);
                // post no agent available
                TasksPool.this.restRequest.postNoAgentAvailable(this.topicId.toString());
            }
            LOGGER.debug("method ended | RequestTtlTimer.run method");
        }
    }
}
