package com.ef.mediaroutingengine.taskmanager.service;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.taskmanager.dto.UpdateTaskRequest;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import com.ef.mediaroutingengine.taskmanager.service.taskservice.TasksRetriever;
import com.ef.mediaroutingengine.taskmanager.service.taskservice.TasksRetrieverFactory;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Tasks service.
 */
@Service
public class TasksService {
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Task retriever factory.
     */
    private final TasksRetrieverFactory tasksRetrieverFactory;
    /**
     * JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Instantiates a new Tasks service.
     *
     * @param tasksPool             the tasks pool
     * @param tasksRetrieverFactory the task retriever factory
     */
    @Autowired
    public TasksService(TasksPool tasksPool, TasksRepository tasksRepository,
                        TasksRetrieverFactory tasksRetrieverFactory, JmsCommunicator jmsCommunicator) {
        this.tasksPool = tasksPool;
        this.tasksRepository = tasksRepository;
        this.tasksRetrieverFactory = tasksRetrieverFactory;
        this.jmsCommunicator = jmsCommunicator;
    }

    /**
     * Retrieve by id task dto.
     *
     * @param taskId the task id
     * @return the task dto
     */
    public TaskDto retrieveById(String taskId) {
        Task task = this.tasksPool.findById(taskId);
        if (task != null) {
            return AdapterUtility.createTaskDtoFrom(task);
        } else {
            throw new NotFoundException("Task not found in Task pool");
        }
    }

    /**
     * Retrieve list.
     *
     * @param agentId   the agent id
     * @param taskState the task state
     * @return the list
     */
    public List<TaskDto> retrieve(Optional<String> agentId,
                                  Optional<Enums.TaskStateName> taskState) {
        TasksRetriever tasksRetriever = this.tasksRetrieverFactory.getRetriever(agentId, taskState);
        return tasksRetriever.findTasks();
    }

    /**
     * Update task task dto.
     *
     * @param taskId  the task id
     * @param reqBody the req body
     * @return the task dto
     */
    public TaskDto updateTask(String taskId, UpdateTaskRequest reqBody) {
        Task task = this.tasksPool.findById(taskId);
        if (task == null) {
            throw new NotFoundException("Task not found in Task pool");
        }

        task.setChannelSession(reqBody.getChannelSession());
        this.tasksRepository.updateChannelSession(taskId, reqBody.getChannelSession());
        jmsCommunicator.publishTaskStateChangeForReporting(task);
        return AdapterUtility.createTaskDtoFrom(task);
    }

    /**
     * Assign task task dto.
     *
     * @param agent          the agent
     * @param mrd            the mrd
     * @param taskState      the task state
     * @param channelSession the channel session
     * @return the task dto
     */
    public TaskDto assignTask(Agent agent, MediaRoutingDomain mrd, TaskState taskState,
                              ChannelSession channelSession) {
        String conversationId = channelSession.getConversationId();

        List<Task> existingTasksOnTopic = tasksPool.findByConversationId(conversationId);
        for (Task task : existingTasksOnTopic) {
            if (task.getAssignedTo().equals(agent.getId())) {
                return AdapterUtility.createTaskDtoFrom(task);
            }
        }

        Task task = createTask(agent, mrd, taskState, channelSession);
        agent.addActiveTask(task);


        return AdapterUtility.createTaskDtoFrom(task);
    }

    private Task createTask(Agent agent, MediaRoutingDomain mrd, TaskState state, ChannelSession channelSession) {
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.AGENT, null);
        Task task = Task.getInstanceFrom(agent.getId(), mrd, state, channelSession, type);

        this.tasksPool.add(task);
        this.tasksRepository.save(task.getId(), AdapterUtility.createTaskDtoFrom(task));
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);

        return task;
    }
}
