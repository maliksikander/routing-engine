package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
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
     * Instantiates a new Assign agent service.
     *
     * @param taskManager the task manager
     * @param restRequest the rest request
     */
    public AssignAgentService(TaskManager taskManager, RestRequest restRequest) {
        this.taskManager = taskManager;
        this.restRequest = restRequest;
    }


    /**
     * Assign.
     *
     * @param conversation   the conversation
     * @param agent          the agent
     * @param mrd            the mrd
     * @param channelSession the channel session
     */
    public void assign(String conversation, Agent agent, MediaRoutingDomain mrd, ChannelSession channelSession) {
        TaskState taskState = new TaskState(Enums.TaskStateName.STARTED, null);

        Task task = Task.getInstanceFrom(agent.getId(), mrd, taskState, channelSession);

        this.taskManager.insertInPoolAndRepository(task);
        this.taskManager.publishTaskForReporting(task);

        this.restRequest.postAssignTask(channelSession, agent.toCcUser(), conversation, task.getId(), taskState);
    }
}
