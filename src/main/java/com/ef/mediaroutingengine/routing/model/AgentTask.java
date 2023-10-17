package com.ef.mediaroutingengine.routing.model;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskQueue;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Agent task.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AgentTask {
    private String taskId;
    private String taskMediaId;
    private String conversationId;
    private String mrdId;
    private Enums.TaskTypeMode mode;
    private TaskQueue queue;

    /**
     * Instantiates a new Agent task.
     *
     * @param task      the task
     * @param taskMedia the task media
     */
    public AgentTask(Task task, TaskMedia taskMedia) {
        this.taskId = task.getId();
        this.taskMediaId = taskMedia.getId();
        this.conversationId = task.getConversationId();
        this.mrdId = taskMedia.getMrdId();
        this.mode = taskMedia.getType().getMode();
        this.queue = taskMedia.getQueue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentTask agentTask = (AgentTask) o;
        return Objects.equals(taskId, agentTask.taskId)
                && Objects.equals(taskMediaId, agentTask.taskMediaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, taskMediaId);
    }
}
