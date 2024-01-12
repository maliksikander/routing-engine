package com.ef.mediaroutingengine.routing.model;

import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.mediaroutingengine.taskmanager.model.TaskStep;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Queue task.
 */
@Getter
@Setter
@ToString
public class QueueTask {
    /**
     * The ID.
     */
    private final String id;
    /**
     * The Conversation id.
     */
    private final String conversationId;
    /**
     * The Task id.
     */
    private final String taskId;
    /**
     * The Media id.
     */
    private final String mediaId;
    /**
     * The Queue id.
     */
    private final String queueId;
    /**
     * The Priority.
     */
    private final int priority;
    /**
     * The Enqueue time.
     */
    private final Long enqueueTime;
    /**
     * The Current step.
     */
    @JsonIgnore
    private TaskStep currentStep;

    /**
     * Instantiates a new Queue task.
     *
     * @param taskId   the task id
     * @param mediaId  the media id
     * @param priority the priority
     */
    public QueueTask(String conversationId, String taskId, String mediaId, String queueId, int priority,
                     long enqueueTime) {
        this.id = UUID.randomUUID().toString();
        this.conversationId = conversationId;
        this.taskId = taskId;
        this.mediaId = mediaId;
        this.queueId = queueId;
        this.priority = priority;
        this.enqueueTime = enqueueTime;
    }

    public QueueTask(String conversationId, TaskMedia media) {
        this(conversationId, media.getTaskId(), media.getId(), media.getQueue().getId(), media.getPriority(),
                media.getEnqueueTime());
    }

    @JsonIgnore
    public String getLastAssignedAgentId() {
        return null;
    }
}
