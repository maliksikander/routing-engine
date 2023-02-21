package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.dto.AssignAgentRequest;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
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

    private final TasksPool tasksPool;

    private final TasksRepository tasksRepository;

    private final RestRequest restRequest;

    /**
     * Instantiates a new Assign agent service.
     *
     * @param taskManager the task manager
     */
    public AssignAgentService(TaskManager taskManager, JmsCommunicator jmsCommunicator,
                              TasksPool tasksPool, TasksRepository tasksRepository, RestRequest restRequest) {
        this.taskManager = taskManager;
        this.jmsCommunicator = jmsCommunicator;
        this.tasksPool = tasksPool;
        this.tasksRepository = tasksRepository;
        this.restRequest = restRequest;
    }


    /**
     * Assign.
     *
     * @param req          the AssignAgentRequest object
     * @param agent        the agent
     * @param mrd          the mrd
     * @param updateTask   the updateTask flag
     * @param offerToAgent the offerToAgent flag
     */
    public TaskDto assign(AssignAgentRequest req, Agent agent, MediaRoutingDomain mrd, boolean updateTask,
                          boolean offerToAgent) {

        Task task = this.getExistingTask(agent.getId(), req.getChannelSession());

        if (task == null) {
            task = Task.getInstanceFrom(agent.getId(), mrd, req.getTaskState(), req.getChannelSession(),
                    req.getTaskType());

            this.taskManager.insertInPoolAndRepository(task);
            this.jmsCommunicator.publishTaskStateChangeForReporting(task);
        } else if (updateTask) {
            this.update(task, req.getChannelSession(), mrd);
        }

        if (offerToAgent) {
            this.restRequest.postAssignTask(task, agent.toCcUser(), task.getTaskState(), true);
        }

        return AdapterUtility.createTaskDtoFrom(task);
    }

    Task getExistingTask(String agentId, ChannelSession channelSession) {
        return this.tasksPool.findByConversationId(channelSession.getConversationId()).stream()
                .filter(task -> task.getAssignedTo() != null && task.getAssignedTo().equals(agentId))
                .findFirst()
                .orElse(null);
    }

    void update(Task existingTask, ChannelSession channelSession, MediaRoutingDomain mrd) {
        existingTask.setChannelSession(channelSession);
        existingTask.setMrd(mrd);
        this.tasksRepository.save(existingTask.getId(), AdapterUtility.createTaskDtoFrom(existingTask));
    }
}
