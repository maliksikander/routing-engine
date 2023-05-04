package com.ef.mediaroutingengine.taskmanager.repository;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.TaskAgent;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.redis.RedisClient;
import com.ef.mediaroutingengine.global.redis.RedisJsonDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The type Tasks repository.
 */
@Component
public class TasksRepository extends RedisJsonDao<TaskDto> {

    /**
     * Instantiates a new Tasks repository.
     *
     * @param redisClient the redis client
     */
    @Autowired
    public TasksRepository(RedisClient redisClient) {
        super(redisClient, "task");
    }

    /**
     * Change state boolean.
     *
     * @param taskId the task id
     * @param state  the state
     * @return the boolean
     */
    public boolean changeState(String taskId, TaskState state) {
        return this.updateField(taskId, ".state", state);
    }

    /**
     * Update assigned to boolean.
     *
     * @param taskId     the task id
     * @param assignedTo the assigned to
     * @return the boolean
     */
    public boolean updateAssignedTo(String taskId, TaskAgent assignedTo) {
        return this.updateField(taskId, ".assignedTo", assignedTo);
    }

    public boolean updateChannelSession(String taskId, ChannelSession channelSession) {
        return this.updateField(taskId, ".channelSession", channelSession);
    }
}
