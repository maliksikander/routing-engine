package com.ef.mediaroutingengine.services;

import com.ef.mediaroutingengine.eventlisteners.DispatchSelectedAgent;
import com.ef.mediaroutingengine.eventlisteners.NewTaskEvent;
import com.ef.mediaroutingengine.eventlisteners.TaskStateEvent;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.CommonEnums;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.TaskService;
import com.ef.mediaroutingengine.model.Tuple;
import com.ef.mediaroutingengine.repositories.PriorityLabelsPool;
import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceManager.class);

    private final PrecisionQueuesPool precisionQueuesPool;
    private final List<TaskService> allTasks;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private final List<PropertyChangeListener> listeners;
    private final List<String> changeSupportPrecisionQueueListeners = new LinkedList<>();

    /**
     * Default constructor. Autowired -> loads Dependencies.
     *
     * @param precisionQueuesPool pool of all precision queues.
     */
    @Autowired
    public TaskServiceManager(PrecisionQueuesPool precisionQueuesPool) {
        this.precisionQueuesPool = precisionQueuesPool;
        this.listeners = new LinkedList<>();
        allTasks = new LinkedList<>();
        this.initialize();
    }

    @Lookup
    public TaskStateEvent getTaskStateEvent() {
        return null;
    }

    private void initialize() {
        listeners.add(new NewTaskEvent());
        listeners.add(new DispatchSelectedAgent());
        listeners.add(getTaskStateEvent());

        for (PropertyChangeListener listener : this.listeners) {
            this.changeSupport.addPropertyChangeListener(listener);
        }
    }

    /**
     * Adds a property change listener, which will listen to property changes of this object.
     *
     * @param listener the property change listener object
     * @param name the name of the listener
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
     * @param task the task assigned to the agent
     */
    public void dispatchSelectedAgent(Agent agent, TaskService task) {
        // Implementation to be completed with respect to new design
        // Dispatching event, Dispatched with event: "TaskAgentSelected" to jms communicator
        // Agent-reserved according to new design.
        LOGGER.info("Dispatching agent: {} assigned to task: {}", agent.getId(), task.getId());
    }

    /**
     * Transfer a task from one agent to another.
     *
     * @param agent the agent the task is being transferred to
     * @param previousAgent the previous agent who was handling the task
     * @param task the task being transferred
     */
    public void transferTask(Agent agent, Agent previousAgent, TaskService task) {
        // Implementation to be completed with respect to new design
        // Dispatching event to either bot-framework or Agent-manager
        LOGGER.info("Transferring Task: {} from agent: {} to agent: {}",
                task.getId(), previousAgent.getId(), agent.getId());
    }

    /**
     * Rejects transferring of task to another agent.
     *
     * @param agent the agent the task was intended to transfer to
     * @param task the task in use
     */
    public void transferReject(Agent agent, TaskService task) {
        // Implementation to be completed with respect to new design
        // Dispatching event to either bot-framework or Agent-manager
        LOGGER.info("Transferring task: {} to agent: {} rejected", task.getId(), agent.getId());
    }

    public void addTaskDuringStateRestore(TaskService taskService) {
        this.allTasks.add(taskService);
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

    private boolean contains(TaskService task) {
        for (TaskService element : this.allTasks) {
            if (element.getId().equalsIgnoreCase(task.getId())) {
                return true;
            }
        }
        return false;
    }

    private void add(TaskService task) {
        if (!this.contains(task)) {
            this.allTasks.add(task);
        }
    }

    private TaskService createTaskInstanceFrom(JsonNode node) {
        String taskId = node.get("TaskId").textValue();
        String mrdName = node.get("MRD").textValue();
        Tuple<Integer, String> priorityTuple = this.getPriorityTupleFrom(node);
        JsonNode params = node.get("Params");
        String lastAssignedAgentId = params.has("agent") ? params.get("agent").textValue() : "";
        String conversationId = params.has("chatId") ? params.get("chatId").textValue() : "";

        return new TaskService(taskId, "", mrdName, priorityTuple.first, priorityTuple.second,
                lastAssignedAgentId, conversationId);
    }

    /**
     * Adds a new task in the tasks pool and enqueue it in the relevant precision queue.
     *
     * @param node Json object containing data to add a new task.
     */
    public void enqueueNewTask(JsonNode node) {
        TaskService task = this.createTaskInstanceFrom(node);
        this.add(task);

        String queueName = node.get("Params").get("skillgroup").textValue();
        PrecisionQueue queue = this.precisionQueuesPool.findByName(queueName);

        // If skill group queue not found, use default queue
        if (queue == null) {
            queueName = CommonEnums.DefaultQueue.DEFAULT_PRECISION_QUEUE.name();
            queue = this.precisionQueuesPool.findByName(queueName);
        }
        queue.enqueue(task);

        task.setQueueName(queueName);
        task.setTimeouts(queue.getTimeouts());
        this.changeSupport.firePropertyChange(CommonEnums.IncomingMsgType.NEW_TASK.name(), null, task);
    }

    /**
     * Enqueues a transfer task in the default precision queue.
     *
     * @param taskService the task being enqueued
     */
    public void enqueueTransferTask(TaskService taskService) {
        this.precisionQueuesPool.getDefaultQueue().enqueue(taskService);
        String eventName = CommonEnums.IncomingMsgType.NEW_TASK.name();
        this.changeSupport.firePropertyChange(eventName, null, taskService);
    }

    public void changeTaskState(JsonNode node) {
        this.changeSupport.firePropertyChange(CommonEnums.IncomingMsgType.TASK_STATE.name(), null, node);
    }

    public void conferenceChat(JsonNode node) {
        this.changeSupport.firePropertyChange(CommonEnums.IncomingMsgType.CONFERENCE.name(), null, node);
    }

    /**
     * Handles the conference of two agents on a task.
     *
     * @param agent main agent assigned to the task
     * @param participantAgent new agent being added to handle the task
     * @param task the task being handled
     */
    public void conference(Agent agent, Agent participantAgent, TaskService task) {
        // Dispatcher event. Published message "TaskConferenced" on communicator
        // To be implemented in the new design.
        LOGGER.debug("Conference method called");
    }

    public List<TaskService> getAllTasks() {
        return this.allTasks;
    }

    /**
     * Returns task by task-id from the tasks pool.
     *
     * @param taskId id of the task to find
     * @return TaskService object if found, null otherwise
     */
    public TaskService getTask(String taskId) {
        for (TaskService task : this.allTasks) {
            if (task.getId().equalsIgnoreCase(taskId)) {
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
    public TaskService getTaskByConversationId(String conversationId) {
        for (TaskService task : this.allTasks) {
            if (task.getConversationId().equalsIgnoreCase(conversationId)) {
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
        for (TaskService task : this.allTasks) {
            if (task.getId().equalsIgnoreCase(taskId)) {
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
