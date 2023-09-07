package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.AgentRequestTimerService;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;

/**
 * The type Task state close.
 */
public class TaskStateClose implements TaskStateModifier {
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    private final AgentRequestTimerService agentRequestTimerService;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param precisionQueuesPool Pool of all precision queues
     * @param taskManager         handles tasks closing.
     */
    public TaskStateClose(PrecisionQueuesPool precisionQueuesPool, TasksPool tasksPool, TaskManager taskManager,
                          JmsCommunicator jmsCommunicator, AgentRequestTimerService agentRequestTimerService) {
        this.precisionQueuesPool = precisionQueuesPool;
        this.tasksPool = tasksPool;
        this.taskManager = taskManager;
        this.jmsCommunicator = jmsCommunicator;
        this.agentRequestTimerService = agentRequestTimerService;
    }


    @Override
    public boolean updateState(Task task, TaskState state) {
        task.setTaskState(state);

        this.precisionQueuesPool.endTask(task);
        this.taskManager.removeFromPoolAndRepository(task);
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);

        if (isRona(state)) {
            this.taskManager.endTaskFromAgentOnRona(task);
            this.taskManager.rerouteReservedTask(task);
            return true;
        }

        if (tasksPool.findInProcessTaskFor(task.getTopicId()) == null) {
            this.agentRequestTimerService.stop(task.getTopicId());
        }

        this.taskManager.endTaskFromAssignedAgent(task);
        return true;
    }

    private boolean isRona(TaskState state) {
        return state.getReasonCode() != null && state.getReasonCode().equals(Enums.TaskStateReasonCode.RONA);
    }
}
