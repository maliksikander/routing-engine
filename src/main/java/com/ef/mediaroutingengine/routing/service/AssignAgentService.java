package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.MrdType;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.mediaroutingengine.global.exceptions.ConflictException;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.locks.ConversationLock;
import com.ef.mediaroutingengine.routing.dto.AssignAgentRequest;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.routing.utility.TaskUtility;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
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
    private final ConversationLock conversationLock = new ConversationLock();

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

        try {
            conversationLock.lock(conversationId);

            if (TaskUtility.isNamedAgentTransfer(req.getType())) {
                return handleNamedAgentTransfer(req, agent, mrdId, conversationId);
            }

            // Get Conversation Tasks | Revoke Auto-JoinAble, InProcess Tasks | Collect the tasks which are not revoked.
            List<Task> tasks = this.tasksRepository.findAllByConversationId(conversationId).stream()
                    .filter(t -> !taskManager.revokeInProcessTask(t, true))
                    .toList();

            Task task = this.getTaskOfAgent(agent, tasks);

            if (task == null) {
                return handleNewTask(req, agent, mrdId, conversationId);
            }

            if (this.mrdPool.getType(mrdId).isAutoJoin()) {
                return task;
            }

            TaskMedia media = task.findMediaByMrdId(mrdId);

            if (media == null) {
                media = this.createMedia(req, task.getId(), mrdId);
                task.addMedia(media);
            } else {
                media.setState(req.getState());
            }

            if (req.getState().equals(TaskMediaState.ACTIVE)) {
                taskManager.activateMedia(task, media);
            } else {
                tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());
                jmsCommunicator.publishTaskStateChanged(task, media.getRequestSession(), false, media.getId());
            }

            if (req.isOfferToAgent()) {
                restRequest.postAssignTask(task, media, req.getState(), agent.toCcUser(), true);
            }

            return task;
        } finally {
            conversationLock.unlock(conversationId);
        }
    }

    /**
     * Handle new task task.
     *
     * @param req   the req
     * @param agent the agent
     * @param mrdId the mrd id
     * @return the task
     */
    private Task handleNewTask(AssignAgentRequest req, Agent agent, String mrdId, String conversationId) {
        TaskMedia media = this.createMedia(req, UUID.randomUUID().toString(), mrdId);
        Task task = TaskUtility.createNewTask(conversationId, media, agent.toTaskAgent());

        this.tasksRepository.insert(task);
        this.jmsCommunicator.publishTaskStateChanged(task, req.getRequestSession(), true, media.getId());

        if (req.getState().equals(TaskMediaState.ACTIVE)) {
            agent.addActiveTask(task, media);

            if (!this.mrdPool.getType(mrdId).isInterruptible()) {
                agent.setNonInterruptible(true);
            }
        }

        if (req.isOfferToAgent()) {
            this.restRequest.postAssignTask(task, media, req.getState(), agent.toCcUser(), true);
        }

        return task;
    }

    private Task handleNamedAgentTransfer(AssignAgentRequest req, Agent agent, String mrdId, String conversationId) {
        // In case client has provided wrong state in the request
        req.setState(TaskMediaState.RESERVED);

        TaskMedia media = this.createMedia(req, UUID.randomUUID().toString(), mrdId);
        Task task = TaskUtility.createNewTask(conversationId, media, agent.toTaskAgent());

        if (agent.isAvailableForReservation(mrdId, conversationId) && agent.reserveTask(task, media)) {
            this.tasksRepository.insert(task);
            this.jmsCommunicator.publishTaskStateChanged(task, req.getRequestSession(), true, media.getId());

            if (req.isOfferToAgent()) {
                this.restRequest.postAssignTask(task, media, req.getState(), agent.toCcUser(), true);
            }

            return task;
        }

        throw new ConflictException("Requested agent is not available");
    }

    private TaskMedia createMedia(AssignAgentRequest req, String taskId, String mrdId) {
        ChannelSession reqSession = req.getRequestSession();
        MrdType mrdType = this.mrdPool.getType(mrdId);
        List<ChannelSession> sessions = TaskUtility.getSessions(req.getChannelSessions(), reqSession, mrdId, mrdType);

        TaskMedia media = new TaskMedia(mrdId, taskId, null, req.getType(), 1, req.getState(), reqSession, sessions);

        if (req.getState().equals(TaskMediaState.ACTIVE)) {
            media.setAnswerTime(System.currentTimeMillis());
        }

        return media;
    }

    private Task getTaskOfAgent(Agent agent, List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getAssignedTo() != null && task.getAssignedTo().getId().equals(agent.getId())) {
                return task;
            }
        }

        return null;
    }
}
