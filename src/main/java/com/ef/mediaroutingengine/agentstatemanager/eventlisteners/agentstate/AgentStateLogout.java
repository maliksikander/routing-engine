package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
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
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Instantiates a new Agent state logout.
     *
     * @param agentPresenceRepository the agent presence repository
     * @param taskManager             the task manager
     */
    @Autowired
    public AgentStateLogout(AgentPresenceRepository agentPresenceRepository, TaskManager taskManager,
                            JmsCommunicator jmsCommunicator) {
        this.agentPresenceRepository = agentPresenceRepository;
        this.taskManager = taskManager;
        this.jmsCommunicator = jmsCommunicator;
    }

    @Override
    public boolean updateState(Agent agent, AgentState newState, boolean isChangedInternally) {
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
        Task reservedTask = agent.getReservedTask();
        if (reservedTask != null) {
            TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.AGENT_LOGOUT);
            reservedTask.setTaskState(taskState);

            this.taskManager.removeFromPoolAndRepository(reservedTask);
            this.jmsCommunicator.publishTaskStateChangeForReporting(reservedTask);
            this.taskManager.rerouteReservedTask(reservedTask);
        }
    }

    void handleActiveTasks(Agent agent) {
        for (Task task : agent.getActiveTasksList()) {
            task.setTaskState(new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.AGENT_LOGOUT));
            this.taskManager.removeFromPoolAndRepository(task);
            this.jmsCommunicator.publishTaskStateChangeForReporting(task);
        }
    }
}
