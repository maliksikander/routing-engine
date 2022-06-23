package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
import java.util.UUID;

/**
 * The type Task state close.
 */
public class TaskStateClose implements TaskStateModifier {
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param precisionQueuesPool Pool of all precision queues
     * @param taskManager         handles tasks closing.
     */
    public TaskStateClose(PrecisionQueuesPool precisionQueuesPool, TaskManager taskManager) {
        this.precisionQueuesPool = precisionQueuesPool;
        this.taskManager = taskManager;
    }


    @Override
    public void updateState(Task task, TaskState state) {
        task.setTaskState(state);

        this.precisionQueuesPool.endTask(task);
        this.taskManager.removeFromPoolAndRepository(task);

        if (state.getReasonCode() == null || !state.getReasonCode().equals(Enums.TaskStateReasonCode.RONA)) {
            if (task.getRoutingMode().equals(RoutingMode.PUSH)) {
                UUID topicId = task.getTopicId();
                this.taskManager.cancelAgentRequestTtlTimerTask(topicId);
                this.taskManager.removeAgentRequestTtlTimerTask(topicId);
            }

            this.taskManager.endTaskFromAssignedAgent(task);
            return;
        }

        // Close with Rona
        this.taskManager.endTaskFromAgentOnRona(task);
        if (task.getRoutingMode().equals(RoutingMode.PUSH)) {
            this.taskManager.rerouteReservedTask(task);
        }
    }
}
