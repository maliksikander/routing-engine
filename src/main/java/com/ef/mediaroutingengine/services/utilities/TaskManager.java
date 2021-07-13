package com.ef.mediaroutingengine.services.utilities;

import com.ef.mediaroutingengine.commons.EFUtils;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import java.beans.PropertyChangeEvent;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class TaskManager {
    private final ApplicationContext applicationContext;
    private final AgentsPool agentsPool;

    /**
     * Default Constructor. Loads the dependencies.
     *
     * @param agentsPool pool of all agents.
     * @param applicationContext to get beans at runtime.
     */
    @Autowired
    public TaskManager(AgentsPool agentsPool, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.agentsPool = agentsPool;

    }

    /**
     * Removes a task from the agent it is associated to. Changes the agent's MRD state wrt to number of
     * tasks left ater the task is removed.
     *
     * @param task task to remove.
     */
    public void endTaskFromAssignedAgent(Task task) {
        UUID assignedTo = task.getAssignedTo();
        Agent agent = this.agentsPool.findById(assignedTo);
        if (agent != null) {
            agent.endTask(task);

            UUID mrdId = task.getMrd().getId();
            Enums.AgentMrdStateName currentMrdState = agent.getAgentMrdState(mrdId).getState();
            int noOfTasks = agent.getNoOfActiveTasks(mrdId);

            if (currentMrdState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY) && noOfTasks < 1) {
                this.fireAgentMrdChangeRequest(agent.getId(), mrdId, Enums.AgentMrdStateName.NOT_READY, true);
            } else if (currentMrdState.equals(Enums.AgentMrdStateName.BUSY)) {
                this.fireAgentMrdChangeRequest(agent.getId(), mrdId, Enums.AgentMrdStateName.ACTIVE, true);
            } else if (currentMrdState.equals(Enums.AgentMrdStateName.ACTIVE) && noOfTasks < 1) {
                this.fireAgentMrdChangeRequest(agent.getId(), mrdId, Enums.AgentMrdStateName.READY, true);
            }
        }
    }

    /**
     * Removes the task from the associated agent when a task is closed with reasonCode Rona. Requests to change
     * the agent's mrd state to not-ready.
     *
     * @param task task to be removed.
     */
    public void endTaskFromAgentOnRona(Task task) {
        UUID assignedTo = task.getAssignedTo();
        Agent agent = this.agentsPool.findById(assignedTo);
        if (agent != null) {
            agent.removeReservedTask();
            AgentState agentState = new AgentState(Enums.AgentStateName.NOT_READY, null);
            this.fireAgentStateChangeRequest(agent.getId(), agentState);
        }
    }

    private void fireAgentMrdChangeRequest(UUID agentId, UUID mrdId, Enums.AgentMrdStateName state, boolean async) {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, Enums.EventName.AGENT_MRD_STATE.name(),
                null, new AgentMrdStateChangeRequest(agentId, mrdId, state));
        AgentMrdStateListener listener = this.applicationContext.getBean(AgentMrdStateListener.class);
        listener.propertyChange(evt, async);
    }

    private void fireAgentStateChangeRequest(UUID agentId, AgentState agentState) {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, Enums.EventName.AGENT_STATE.name(),
                null, new AgentStateChangeRequest(agentId, agentState));
        AgentStateListener listener = this.applicationContext.getBean(AgentStateListener.class);
        listener.propertyChange(evt);
    }

    /**
     * Updates the Agent's MRD state, when task state changes to active.
     *
     * @param agent agent to b updated
     */
    public void updateAgentMrdState(Agent agent, UUID mrdId) {
        int noOfActiveTasks = agent.getNoOfActiveTasks(mrdId);

        if (noOfActiveTasks == 1) {
            this.fireAgentMrdChangeRequest(agent.getId(), mrdId, Enums.AgentMrdStateName.ACTIVE, false);
        } else if (noOfActiveTasks == EFUtils.MAX_TASKS) {
            this.fireAgentMrdChangeRequest(agent.getId(), mrdId, Enums.AgentMrdStateName.BUSY, false);
        }
    }
}
