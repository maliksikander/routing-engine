package com.ef.mediaroutingengine.services.pools;

import com.ef.cim.objectmodel.ChannelConfig;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.utilities.RestRequest;
import com.fasterxml.jackson.databind.JsonNode;
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

@Service
public class TasksPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(TasksPool.class);

    private final PrecisionQueuesPool precisionQueuesPool;
    private final MrdPool mrdPool;
    private final TasksRepository tasksRepository;
    private final RestRequest restRequest;
    private final List<Task> allTasks;
    private final Map<UUID, RequestTtlTimer> requestTtlTimers;

    private final PropertyChangeSupport changeSupport;
    private final List<String> changeSupportPrecisionQueueListeners = new LinkedList<>();

    /**
     * Default constructor. Autowired -> loads Dependencies.
     *
     * @param precisionQueuesPool pool of all precision queues.
     */
    @Autowired
    public TasksPool(PrecisionQueuesPool precisionQueuesPool, MrdPool mrdPool,
                     TasksRepository tasksRepository, RestRequest restRequest) {
        this.precisionQueuesPool = precisionQueuesPool;
        this.mrdPool = mrdPool;
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

    public void removePropertyChangeListener(PropertyChangeListener listener, String name) {
        this.changeSupportPrecisionQueueListeners.remove(name);
        this.changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Dispatches agent to the relevant component after Agent is reserved.
     *
     * @param agent the agent to dispatch
     * @param task  the task assigned to the agent
     */
    public void dispatchSelectedAgent(Agent agent, Task task) {
        // Implementation to be completed with respect to new design
        // Dispatching event, Dispatched with event: "TaskAgentSelected" to jms communicator
        // Agent-reserved according to new design.
        LOGGER.info("Dispatching agent: {} assigned to task: {}", agent.getId(), task.getId());
    }

    /**
     * Transfer a task from one agent to another.
     *
     * @param agent         the agent the task is being transferred to
     * @param previousAgent the previous agent who was handling the task
     * @param task          the task being transferred
     */
    public void transferTask(Agent agent, Agent previousAgent, Task task) {
        // Implementation to be completed with respect to new design
        // Dispatching event to either bot-framework or Agent-manager
        LOGGER.info("Transferring Task: {} from agent: {} to agent: {}",
                task.getId(), previousAgent.getId(), agent.getId());
    }

    /**
     * Rejects transferring of task to another agent.
     *
     * @param agent the agent the task was intended to transfer to
     * @param task  the task in use
     */
    public void transferReject(Agent agent, Task task) {
        // Implementation to be completed with respect to new design
        // Dispatching event to either bot-framework or Agent-manager
        LOGGER.info("Transferring task: {} to agent: {} rejected", task.getId(), agent.getId());
    }

    public void addTaskDuringStateRestore(Task task) {
        this.allTasks.add(task);
    }

    private boolean contains(Task task) {
        for (Task element : this.allTasks) {
            if (element.getId().equals(task.getId())) {
                return true;
            }
        }
        return false;
    }

    private void add(Task task) {
        if (!this.contains(task)) {
            this.allTasks.add(task);
        }
    }

    private MediaRoutingDomain getMediaRoutingDomainFrom(AssignResourceRequest request) {
        LOGGER.debug("method started | TasksPool.getMediaRoutingDomainFrom");
        ChannelSession channelSession = request.getChannelSession();
        UUID mrdId = channelSession.getChannel().getChannelConnector().getChannelType().getMediaRoutingDomain();
        LOGGER.debug("method ended | TasksPool.getMediaRoutingDomainFrom");
        return this.mrdPool.findById(mrdId);
    }

    private Task createTaskInstanceFrom(AssignResourceRequest request, PrecisionQueue queue) {
        LOGGER.debug("method started | TasksPool.createTaskInstanceFrom method");
        MediaRoutingDomain mrd = this.getMediaRoutingDomainFrom(request);
        Task task = new Task(request.getChannelSession(), mrd, queue.getId());
        LOGGER.debug("method ended | TasksPool.createTaskInstanceFrom method");
        return task;
    }

    private PrecisionQueue getPrecisionQueueFrom(AssignResourceRequest request) {
        PrecisionQueue queue = this.precisionQueuesPool.findById(request.getQueue());
        // If skill group queue not found, use default queue
        if (queue == null) {
            ChannelConfig channelConfig = request.getChannelSession().getChannel().getChannelConfig();
            queue = this.precisionQueuesPool.findById(channelConfig.getRoutingPolicy().getDefaultQueue());
        }
        return queue;
    }

    private long getDelay(ChannelSession channelSession) {
        int ttl = channelSession.getChannel().getChannelConfig().getRoutingPolicy().getAgentRequestTtl();
        return ttl * 1000L;
    }

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
     * Adds a new task in the tasks pool and enqueue it in the relevant precision queue.
     *
     * @param request request object to assign agent.
     */
    public void enqueueTask(AssignResourceRequest request) {
        LOGGER.debug("method started | TasksPool.enqueueTask method");
        PrecisionQueue queue = this.getPrecisionQueueFrom(request);
        if (queue != null) {
            LOGGER.debug("Precision-Queue is not null | TasksPool.enqueueTask method");
            Task task = this.createTaskInstanceFrom(request, queue);
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
        } else {
            LOGGER.warn("Queue id: {} not found in pool while enqueuing task", request.getQueue());
        }
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

    private void removeOldTaskForReroute(Task task) {
        this.tasksRepository.deleteById(task.getId().toString());
        this.allTasks.remove(task);
    }

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

    private void rerouteActiveTask(Task currentTask) {
        this.removeOldTaskForReroute(currentTask);
        Task newTask = this.setUpNewTaskForReroute(currentTask);
        this.scheduleAgentRequestTimeoutTask(newTask.getChannelSession());
        this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, newTask);
    }

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
     * Enqueues a transfer task in the default precision queue.
     *
     * @param task the task being enqueued
     */
    public void enqueueTransferTask(Task task) {
        this.precisionQueuesPool.getDefaultQueue().enqueue(task);
        String eventName = Enums.EventName.NEW_TASK.name();
        this.changeSupport.firePropertyChange(eventName, null, task);
    }

    public void conferenceChat(JsonNode node) {
        this.changeSupport.firePropertyChange(Enums.EventName.CONFERENCE.name(), null, node);
    }

    /**
     * Handles the conference of two agents on a task.
     *
     * @param agent            main agent assigned to the task
     * @param participantAgent new agent being added to handle the task
     * @param task             the task being handled
     */
    public void conference(Agent agent, Agent participantAgent, Task task) {
        // Dispatcher event. Published message "TaskConferenced" on communicator
        // To be implemented in the new design.
        LOGGER.debug("Conference method called");
    }

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
     * @param conversationId the conversation-id to serch task by
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

    public void removeAgentRequestTtlTimerTask(UUID topicId) {
        this.requestTtlTimers.remove(topicId);
    }

    public int size() {
        return this.allTasks.size();
    }

    public List<UUID> getAllActiveTimers() {
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, RequestTtlTimer> entry: this.requestTtlTimers.entrySet()) {
            result.add(entry.getValue().topicId);
        }
        return result;
    }

    private class RequestTtlTimer extends TimerTask {
        private final UUID topicId;

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
