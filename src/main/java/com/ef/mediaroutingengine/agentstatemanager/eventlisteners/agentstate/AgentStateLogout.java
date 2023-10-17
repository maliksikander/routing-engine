package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.AgentTask;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.ArrayList;
import java.util.List;
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
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;

    /**
     * Instantiates a new Agent state logout.
     *
     * @param agentPresenceRepository the agent presence repository
     * @param taskManager             the task manager
     * @param tasksRepository         the tasks repository
     */
    @Autowired
    public AgentStateLogout(AgentPresenceRepository agentPresenceRepository, TaskManager taskManager,
                            TasksRepository tasksRepository) {
        this.agentPresenceRepository = agentPresenceRepository;
        this.taskManager = taskManager;
        this.tasksRepository = tasksRepository;
    }

    @Override
    public AgentStateChangedResponse updateState(Agent agent, AgentState newState, boolean isChangedInternally) {
        agent.setState(newState);
        this.handleAgentTasks(agent);

        List<String> mrdStateChanges = new ArrayList<>();

        for (AgentMrdState agentMrdState : agent.getAgentMrdStates()) {
            if (!agentMrdState.getState().equals(Enums.AgentMrdStateName.LOGOUT)) {
                agentMrdState.setState(Enums.AgentMrdStateName.LOGOUT);
                mrdStateChanges.add(agentMrdState.getMrd().getId());
            }
        }
        this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
        this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
        return new AgentStateChangedResponse(null, true, mrdStateChanges);
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

    /**
     * Handle reserved tasks.
     *
     * @param agent the agent
     */
    void handleReservedTasks(Agent agent) {
        AgentTask reservedTask = agent.getReservedTask();
        if (reservedTask != null) {
            TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.AGENT_LOGOUT);
            Task task = this.tasksRepository.find(reservedTask.getTaskId());
            this.taskManager.reroute(task, taskState);
        }
    }

    /**
     * Handle active tasks.
     *
     * @param agent the agent
     */
    void handleActiveTasks(Agent agent) {
        List<String> taskIds = agent.getActiveTasksList().stream()
                .map(AgentTask::getTaskId)
                .toList();

        List<Task> activeTasks = this.tasksRepository.findAll(taskIds);


        for (Task task : activeTasks) {
            TaskState state = new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.AGENT_LOGOUT);
            this.taskManager.closeTask(task, state);
        }
    }
}
