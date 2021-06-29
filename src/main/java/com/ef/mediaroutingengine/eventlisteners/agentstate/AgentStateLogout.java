package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateLogout implements AgentStateDelegate {
    private final AgentPresenceRepository agentPresenceRepository;
    private final TasksPool tasksPool;

    @Autowired
    public AgentStateLogout(AgentPresenceRepository agentPresenceRepository, TasksPool tasksPool) {
        this.agentPresenceRepository = agentPresenceRepository;
        this.tasksPool = tasksPool;
    }

    @Override
    public boolean updateState(Agent agent, AgentState newState) {
        agent.setState(newState);
        this.rerouteAllTasksOf(agent);
        for (AgentMrdState agentMrdState : agent.getAgentMrdStates()) {
            agentMrdState.setState(Enums.AgentMrdStateName.LOGOUT);
        }
        this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
        this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
        return true;
    }

    private void rerouteAllTasksOf(Agent agent) {
        for (Task task : agent.getAllTasks()) {
            this.tasksPool.rerouteTask(task);
        }
        agent.clearAllTasks();
    }
}
