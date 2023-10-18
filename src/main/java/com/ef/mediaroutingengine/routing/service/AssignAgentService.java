package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskAgent;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.dto.AssignAgentRequest;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.AgentTask;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The type Assign agent service.
 */
@Service
public class AssignAgentService {
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Rest request.
     */
    private final RestRequest restRequest;
    /**
     * The Mrd pool.
     */
    private final MrdPool mrdPool;

    /**
     * Instantiates a new Assign agent service.
     *
     * @param taskManager     the task manager
     * @param jmsCommunicator the jms communicator
     * @param tasksRepository the tasks repository
     * @param restRequest     the rest request
     * @param mrdPool         the mrd pool
     */
    public AssignAgentService(TaskManager taskManager, JmsCommunicator jmsCommunicator,
                              TasksRepository tasksRepository, RestRequest restRequest,
                              MrdPool mrdPool) {
        this.taskManager = taskManager;
        this.jmsCommunicator = jmsCommunicator;
        this.tasksRepository = tasksRepository;
        this.restRequest = restRequest;
        this.mrdPool = mrdPool;
    }

    /**
     * Assign task.
     *
     * @param req   the req
     * @param agent the agent
     * @return the task
     */
    public Task assign(AssignAgentRequest req, Agent agent) {
        String mrdId = req.getRequestSession().getChannel().getChannelType().getMediaRoutingDomain();
        String conversationId = req.getRequestSession().getConversationId();
        AgentTask agentTask = agent.getTaskByConversationId(conversationId);

        if (agentTask == null) {
            return handleNewTask(req, agent, mrdId, conversationId);
        }

        Task task = this.tasksRepository.find(agentTask.getTaskId());

        if (this.mrdPool.getType(mrdId).isAutoJoin()) {
            return task;
        }

        boolean isTaskRevoked = this.taskManager.revokeInProcessTask(task);
        if (isTaskRevoked) {
            return handleNewTask(req, agent, mrdId, conversationId);
        }

        TaskMedia taskMedia = task.findMediaByMrdId(mrdId);

        if (taskMedia == null) {
            taskMedia = createMedia(req, task.getId(), mrdId);
            task.addMedia(taskMedia);
            this.tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());
        } else {
            taskMedia.setState(req.getState());
        }

        if (req.getState().equals(TaskMediaState.ACTIVE)) {
            this.taskManager.activateMedia(task, taskMedia);
        } else {
            this.jmsCommunicator.publishTaskMediaStateChanged(task.getConversationId(), taskMedia);
        }

        if (req.isOfferToAgent()) {
            restRequest.postAssignTask(task, taskMedia, req.getState(), agent.toCcUser(), true);
        }

        return task;
    }

    /**
     * Handle new task task.
     *
     * @param req            the req
     * @param agent          the agent
     * @param mrdId          the mrd id
     * @param conversationId the conversation id
     * @return the task
     */
    private Task handleNewTask(AssignAgentRequest req, Agent agent, String mrdId, String conversationId) {
        Task task = this.createTask(req, agent.toTaskAgent(), conversationId, mrdId);

        if (req.getState().equals(TaskMediaState.ACTIVE) && !this.mrdPool.getType(mrdId).isInterruptible()) {
            agent.setNonInterruptible(true);
        }

        if (req.isOfferToAgent()) {
            TaskMedia media = task.findMediaByMrdId(mrdId);
            this.restRequest.postAssignTask(task, media, req.getState(), agent.toCcUser(), true);
        }

        return task;
    }

    /**
     * Create task task.
     *
     * @param req            the req
     * @param agent          the agent
     * @param conversationId the conversation id
     * @param mrdId          the mrd id
     * @return the task
     */
    private Task createTask(AssignAgentRequest req, TaskAgent agent, String conversationId, String mrdId) {
        String taskId = UUID.randomUUID().toString();

        TaskMedia media = createMedia(req, taskId, mrdId);
        List<TaskMedia> medias = new ArrayList<>();
        medias.add(media);

        TaskState taskState = new TaskState(Enums.TaskStateName.ACTIVE, null);
        Task task = new Task(taskId, conversationId, taskState, agent, null, medias);

        this.tasksRepository.save(taskId, task);

        this.jmsCommunicator.publishTaskStateChanged(task, req.getRequestSession());
        task.getActiveMedia().forEach(m -> this.jmsCommunicator.publishTaskMediaStateChanged(conversationId, m));

        return task;
    }

    /**
     * Create media task media.
     *
     * @param req    the req
     * @param taskId the task id
     * @param mrdId  the mrd id
     * @return the task media
     */
    private TaskMedia createMedia(AssignAgentRequest req, String taskId, String mrdId) {
        List<ChannelSession> sessions = req.getChannelSessions().stream()
                .filter(c -> c.getChannel().getChannelType().getMediaRoutingDomain().equals(mrdId)).toList();

        return new TaskMedia(mrdId, taskId, null, req.getType(), 1, req.getState(),
                req.getRequestSession(), sessions);
    }
}
