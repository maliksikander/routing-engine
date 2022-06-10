package com.ef.mediaroutingengine.services.controllerservices.taskservice;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.dto.UpdateTaskRequest;
import com.ef.mediaroutingengine.exceptions.ConflictException;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    public TaskDto retrieveById(UUID taskId) {
        Task task = this.tasksPool.findById(taskId);
        if (task != null) {
            return new TaskDto(task);
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
    public List<TaskDto> retrieve(Optional<UUID> agentId, Optional<Enums.TaskStateName> taskState) {
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
    public TaskDto updateTask(UUID taskId, UpdateTaskRequest reqBody) {
        Task task = this.tasksPool.findById(taskId);
        if (task == null) {
            throw new NotFoundException("Task not found in Task pool");
        }

        task.setChannelSession(reqBody.getChannelSession());
        this.tasksRepository.updateChannelSession(taskId, reqBody.getChannelSession());
        jmsCommunicator.publishTaskStateChangeForReporting(task);
        return new TaskDto(task);
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
        if (channelSession != null) {
            return assignPullTask(agent, mrd, taskState, channelSession);
        } else {
            return assignExternalTask(agent, mrd, taskState);
        }
    }

    private TaskDto assignPullTask(Agent agent, MediaRoutingDomain mrd,
                                   TaskState taskState, ChannelSession channelSession) {
        UUID conversationId = channelSession.getConversationId();

        List<Task> existingTasksOnTopic = tasksPool.findByConversationId(conversationId);
        for (Task task : existingTasksOnTopic) {
            if (task.getAssignedTo().equals(agent.getId()) && task.getRoutingMode().equals(RoutingMode.PULL)) {
                return new TaskDto(task);
            }
        }

        Task task = createTask(agent, mrd, taskState, channelSession);
        agent.addActiveTask(task);
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);

        return new TaskDto(task);
    }

    private TaskDto assignExternalTask(Agent agent, MediaRoutingDomain mrd, TaskState taskState) {
//        if (agent.getVoiceReservedTask() != null) {
//            throw new ConflictException("This Agent is already reserved for an external task");
//        }

        Task task = createTask(agent, mrd, taskState, null);
        agent.setVoiceReservedTask(task);
        return new TaskDto(task);
    }

    private Task createTask(Agent agent, MediaRoutingDomain mrd, TaskState state, ChannelSession channelSession) {
        Task task = Task.getInstanceFrom(agent.getId(), mrd, state, channelSession);
        this.tasksPool.add(task);
        this.tasksRepository.save(task.getId().toString(), new TaskDto(task));
        return task;
    }
}
