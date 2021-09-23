package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.cim.objectmodel.RoutingMode;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentState;
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
        for (Task task : agent.getAllTasks()) {
            RoutingMode routingMode = task.getRoutingMode();
            if (routingMode.equals(RoutingMode.PUSH)) {
                this.taskManager.rerouteTask(task);
            } else if (routingMode.equals(RoutingMode.PULL)) {
                this.taskManager.removePullTaskOnAgentLogout(task);
            }
        }
        agent.clearAllTasks();
    }
}
