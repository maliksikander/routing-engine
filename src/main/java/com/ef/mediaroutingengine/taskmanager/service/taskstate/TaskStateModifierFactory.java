package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Task state modifier factory.
 */
@Service
public class TaskStateModifierFactory {
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    /**
     * The Agent state listener.
     */
    private final AgentStateListener agentStateListener;
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;

    /**
     * Instantiates a new Task state modifier factory.
     *
     * @param tasksRepository    the tasks repository
     * @param agentsPool         the agents pool
     * @param jmsCommunicator    the jms communicator
     * @param agentStateListener the agent state listener
     * @param taskManager        the task manager
     */
    @Autowired
    public TaskStateModifierFactory(TasksRepository tasksRepository, AgentsPool agentsPool,
                                    JmsCommunicator jmsCommunicator, AgentStateListener agentStateListener,
                                    TaskManager taskManager) {
        this.tasksRepository = tasksRepository;
        this.agentsPool = agentsPool;
        this.jmsCommunicator = jmsCommunicator;
        this.agentStateListener = agentStateListener;
        this.taskManager = taskManager;
    }

    /**
     * Returns the Task state modifier object for the requested state change.
     *
     * @param state the requested Task state to be changed
     * @return Task State Modifier for the requested state change
     */
    public TaskStateModifier getModifier(Enums.TaskStateName state) {
        if (state.equals(Enums.TaskStateName.CLOSED)) {
            return new TaskStateClose(agentsPool, agentStateListener, taskManager);
        } else if (state.equals(Enums.TaskStateName.WRAP_UP)) {
            return new TaskStateWrapUp(tasksRepository, jmsCommunicator);
        } else {
            return null;
        }
    }
}
