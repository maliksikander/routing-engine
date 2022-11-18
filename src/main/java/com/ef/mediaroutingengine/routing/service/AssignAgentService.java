package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.dto.AssignAgentRequest;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.List;
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
     * The Rest request.
     */
    private final RestRequest restRequest;
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    private final TasksPool tasksPool;

    private final TasksRepository tasksRepository;

    /**
     * Instantiates a new Assign agent service.
     *
     * @param taskManager the task manager
     * @param restRequest the rest request
     */
    public AssignAgentService(TaskManager taskManager, RestRequest restRequest, JmsCommunicator jmsCommunicator,
                              TasksPool tasksPool, TasksRepository tasksRepository) {
        this.taskManager = taskManager;
        this.restRequest = restRequest;
        this.jmsCommunicator = jmsCommunicator;
        this.tasksPool = tasksPool;
        this.tasksRepository = tasksRepository;
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
    public void assign(AssignAgentRequest req, Agent agent, MediaRoutingDomain mrd, boolean updateTask,
                       boolean offerToAgent) {

        Task existingTask = this.getExistingTask(agent.getId(), req.getChannelSession());

        // No Existing Task, Create a new Task for this request
        if (existingTask == null) {
            Task task = Task.getInstanceFrom(agent.getId(), mrd, req.getTaskState(), req.getChannelSession(),
                    req.getTaskType());

            this.taskManager.insertInPoolAndRepository(task);
            this.jmsCommunicator.publishTaskStateChangeForReporting(task);

            if (offerToAgent) {
                this.restRequest.postAssignTask(task, agent.toCcUser(), req.getTaskState());
            }
        } else if (updateTask) {
            this.update(existingTask, req.getChannelSession(), mrd);
        }
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
