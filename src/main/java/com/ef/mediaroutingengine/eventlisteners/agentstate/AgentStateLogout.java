package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Agent state logout.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateLogout implements AgentStateDelegate {
    /**
     * The Agent presence repository.
     */
    private final AgentPresenceRepository agentPresenceRepository;

    /**
     * The Task manager.
     */
    private final TaskManager taskManager;

    /**
     * Instantiates a new Agent state logout.
     *
     * @param agentPresenceRepository the agent presence repository
     * @param taskManager             the task manager
     */
    @Autowired
    public AgentStateLogout(AgentPresenceRepository agentPresenceRepository, TaskManager taskManager) {
        this.agentPresenceRepository = agentPresenceRepository;
        this.taskManager = taskManager;
    }

    @Override
    public boolean updateState(Agent agent, AgentState newState) {
        agent.setState(newState);
        this.handleAgentTasks(agent);
        for (AgentMrdState agentMrdState : agent.getAgentMrdStates()) {
            agentMrdState.setState(Enums.AgentMrdStateName.LOGOUT);
        }
        this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
        this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
        return true;
    }

    /**
     * Handle all tasks of.
     *
     * @param agent the agent
     */
    void handleAgentTasks(Agent agent) {
        handleReservedTasks(agent);
        handleActiveTasks(agent);
        agent.clearAllTasks();
    }

    void handleReservedTasks(Agent agent) {
        TaskState closeTaskState = new TaskState(Enums.TaskStateName.CLOSED, null);

        Task nonVoiceReservedTask = agent.getReservedTask();
        if (nonVoiceReservedTask != null) {
            nonVoiceReservedTask.setTaskState(closeTaskState);
            this.taskManager.removeFromPoolAndRepository(nonVoiceReservedTask);
            this.taskManager.rerouteReservedTask(nonVoiceReservedTask);
        }

        Task voiceReservedTask = agent.getVoiceReservedTask();
        if (voiceReservedTask != null) {
            voiceReservedTask.setTaskState(closeTaskState);
            this.taskManager.removeFromPoolAndRepository(voiceReservedTask);
        }
    }

    void handleActiveTasks(Agent agent) {
        for (Task task : agent.getActiveTasksList()) {
            this.taskManager.removeTaskOnAgentLogout(task);
        }
    }
}
