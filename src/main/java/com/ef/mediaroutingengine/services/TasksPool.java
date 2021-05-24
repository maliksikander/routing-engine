package com.ef.mediaroutingengine.services;

import com.ef.cim.objectmodel.ChannelConfig;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.Tuple;
import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TasksPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(TasksPool.class);

    private final PrecisionQueuesPool precisionQueuesPool;
    private final MrdPool mrdPool;
    private final List<Task> allTasks;

    private final PropertyChangeSupport changeSupport;
    private final List<String> changeSupportPrecisionQueueListeners = new LinkedList<>();

    /**
     * Default constructor. Autowired -> loads Dependencies.
     *
     * @param precisionQueuesPool pool of all precision queues.
     */
    @Autowired
    public TasksPool(PrecisionQueuesPool precisionQueuesPool, MrdPool mrdPool) {
        this.precisionQueuesPool = precisionQueuesPool;
        this.mrdPool = mrdPool;
        this.allTasks = new LinkedList<>();

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

    private Tuple<Integer, String> getPriorityTupleFrom(JsonNode node) {
        JsonNode params = node.get("Params");
        int priority = Integer.parseInt(node.get("Priority").toString());
        String selectedPriorityLabel = "";

        if (priority == 0 && params != null) {
            Iterator<Map.Entry<String, JsonNode>> paramsIterator = params.fields();
            while (paramsIterator.hasNext()) {
                Map.Entry<String, JsonNode> jsonNode = paramsIterator.next();
                if (jsonNode.getKey().equalsIgnoreCase("labels")) {
                    String[] labelsList = jsonNode.getValue().textValue().split(",");
                    for (String label : labelsList) {
                        PriorityLabelsPool pool = PriorityLabelsPool.getInstance();
                        label = label.toUpperCase();
                        if (pool.contains(label) && pool.getPriority(label) > priority) {
                            priority = pool.getPriority(label);
                            selectedPriorityLabel = label;
                        }
                    }
                }
            }
        }

        return new Tuple<>(priority, selectedPriorityLabel);
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
        ChannelSession channelSession = request.getChannelSession();
        UUID mrdId = channelSession.getChannel().getChannelConnector().getChannelType().getMediaRoutingDomain();
        return this.mrdPool.getMrd(mrdId.toString());
    }

    private Task createTaskInstanceFrom(AssignResourceRequest request, PrecisionQueue queue) {
        MediaRoutingDomain mrd = this.getMediaRoutingDomainFrom(request);
        return new Task(request.getChannelSession(), mrd, queue, "");
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

    /**
     * Adds a new task in the tasks pool and enqueue it in the relevant precision queue.
     *
     * @param request request object to assign agent.
     */
    public void enqueueNewTask(AssignResourceRequest request) {
        PrecisionQueue queue = this.getPrecisionQueueFrom(request);
        Task task = this.createTaskInstanceFrom(request, queue);

        this.add(task);
        queue.enqueue(task);
        task.setTimeouts(queue.getTimeouts());

        this.changeSupport.firePropertyChange(Enums.EventName.NEW_TASK.name(), null, task);
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

    public List<Task> getAllTasks() {
        return this.allTasks;
    }

    /**
     * Returns task by task-id from the tasks pool.
     *
     * @param taskId id of the task to find
     * @return TaskService object if found, null otherwise
     */
    public Task getTask(UUID taskId) {
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
    public Task getTaskByConversationId(UUID conversationId) {
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
     * @param taskId the id of the task to be removed
     * @return true if found and removed, false otherwise
     */
    public boolean removeTask(String taskId) {
        LOGGER.debug("Going to remove task: {}", taskId);
        boolean result = false;
        for (Task task : this.allTasks) {
            if (task.getId().equals(taskId)) {
                this.allTasks.remove(task);
                result = true;
                break;
            }
        }
        LOGGER.debug(result ? "Task with id: {} removed successfully" : "Task not found. Task Id: {}",
                taskId, taskId);
        return result;
    }
}
