package com.ef.mediaroutingengine.global.utilities;

import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.routing.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.taskmanager.model.Task;

/**
 * The type Adapter utility.
 */
public final class AdapterUtility {
    /**
     * Instantiates a new Adapter utility.
     */
    private AdapterUtility() {

    }

    /**
     * Task to task dto task dto.
     *
     * @param task the task
     * @return the task dto
     */
    public static TaskDto createTaskDtoFrom(Task task) {
        TaskDto taskDto = new TaskDto();
        taskDto.setId(task.getId());

        taskDto.setChannelSession(task.getChannelSession());
        taskDto.setMrd(task.getMrd());
        taskDto.setQueue(task.getQueue());

        taskDto.setPriority(task.getPriority());
        taskDto.setState(task.getTaskState());
        taskDto.setAssignedTo(task.getAssignedTo());

        taskDto.setEnqueueTime(task.getEnqueueTime());
        taskDto.setAnswerTime(task.getStartTime());
        taskDto.setHandleTime(task.getHandlingTime());
        taskDto.setType(task.getType());
        return taskDto;
    }

    /**
     * To precision queue entity precision queue entity.
     *
     * @param requestBody the request body
     * @return the precision queue entity
     */
    public static PrecisionQueueEntity createQueueEntityFrom(PrecisionQueueRequestBody requestBody) {
        PrecisionQueueEntity entity = new PrecisionQueueEntity();
        entity.setName(requestBody.getName());
        entity.setMrd(requestBody.getMrd());
        entity.setServiceLevelType(requestBody.getServiceLevelType());
        entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());
        return entity;
    }

    /**
     * Update queue.
     *
     * @param entity      the entity
     * @param requestBody the request body
     */
    public static void updateQueueEntityFrom(PrecisionQueueRequestBody requestBody, PrecisionQueueEntity entity) {
        entity.setName(requestBody.getName());
        entity.setMrd(requestBody.getMrd());
        entity.setServiceLevelType(requestBody.getServiceLevelType());
        entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());
    }
}