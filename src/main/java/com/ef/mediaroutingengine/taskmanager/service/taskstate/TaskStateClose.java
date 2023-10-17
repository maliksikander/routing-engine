package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;

/**
 * The type Task state close.
 */
public class TaskStateClose implements TaskStateModifier {
    /**
     * The Precision queues pool.
     */
    private final AgentsPool agentsPool;
    private final AgentStateListener agentStateListener;
    private final TaskManager taskManager;

    /**
     * Instantiates a new Task state close.
     *
     * @param agentsPool         the agents pool
     * @param agentStateListener the agent state listener
     * @param taskManager        the task manager
     */
    public TaskStateClose(AgentsPool agentsPool, AgentStateListener agentStateListener, TaskManager taskManager) {
        this.agentsPool = agentsPool;
        this.agentStateListener = agentStateListener;
        this.taskManager = taskManager;
    }


    @Override
    public boolean updateState(Task task, TaskState state) {
        if (isRona(state)) {
            this.handleRona(task, state);
            return true;
        }

        this.taskManager.closeTask(task, state);
        return true;
    }

    private boolean isRona(TaskState state) {
        return state.getReasonCode() != null && state.getReasonCode().equals(Enums.TaskStateReasonCode.RONA);
    }

    private void handleRona(Task task, TaskState state) {
        Agent agent = this.agentsPool.findBy(task.getAssignedTo());
        AgentState agentState = new AgentState(Enums.AgentStateName.NOT_READY, null);
        this.agentStateListener.propertyChange(agent, agentState, true);

        this.taskManager.reroute(task, state);
    }
}
