package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;

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
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param precisionQueuesPool Pool of all precision queues
     * @param taskManager         handles tasks closing.
     */
    public TaskStateClose(PrecisionQueuesPool precisionQueuesPool, TaskManager taskManager,
                          JmsCommunicator jmsCommunicator) {
        this.precisionQueuesPool = precisionQueuesPool;
        this.taskManager = taskManager;
        this.jmsCommunicator = jmsCommunicator;
    }


    @Override
    public void updateState(Task task, TaskState state) {
        task.setTaskState(state);

        this.precisionQueuesPool.endTask(task);
        this.taskManager.removeFromPoolAndRepository(task);
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);

        if (state.getReasonCode() == null || !state.getReasonCode().equals(Enums.TaskStateReasonCode.RONA)) {
            if (task.getRoutingMode().equals(RoutingMode.PUSH)) {
                String topicId = task.getTopicId();
                this.taskManager.cancelAgentRequestTtlTimerTask(topicId);
                this.taskManager.removeAgentRequestTtlTimerTask(topicId);
            }

            this.taskManager.endTaskFromAssignedAgent(task);
            return;
        }

        // Close with Rona
        if (task.getRoutingMode().equals(RoutingMode.PUSH)) {
            this.taskManager.endTaskFromAgentOnRona(task);
            this.taskManager.rerouteReservedTask(task);
        }
    }
}
