package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
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

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param precisionQueuesPool Pool of all precision queues
     * @param taskManager         handles tasks closing.
     */
    public TaskStateClose(PrecisionQueuesPool precisionQueuesPool, TasksPool tasksPool, TaskManager taskManager,
                          JmsCommunicator jmsCommunicator) {
        this.precisionQueuesPool = precisionQueuesPool;
        this.tasksPool = tasksPool;
        this.taskManager = taskManager;
        this.jmsCommunicator = jmsCommunicator;
    }


    @Override
    public void updateState(Task task, TaskState state) {
        task.setTaskState(state);

        this.precisionQueuesPool.endTask(task);
        this.taskManager.removeFromPoolAndRepository(task);
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);

        if (isRona(state)) {
            this.taskManager.endTaskFromAgentOnRona(task);
            this.taskManager.rerouteReservedTask(task);
            return;
        }

        String conversationId = task.getTopicId();

        if (tasksPool.findInProcessTaskFor(conversationId) == null) {
            this.taskManager.cancelAgentRequestTtlTimerTask(conversationId);
            this.taskManager.removeAgentRequestTtlTimerTask(conversationId);
        }

        this.taskManager.endTaskFromAssignedAgent(task);
    }

    private boolean isRona(TaskState state) {
        return state.getReasonCode() != null && state.getReasonCode().equals(Enums.TaskStateReasonCode.RONA);
    }
}
