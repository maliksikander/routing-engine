package com.ef.mediaroutingengine.taskmanager.repository;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.redis.RedisClient;
import com.ef.mediaroutingengine.global.redis.RedisJsonDao;
import java.util.UUID;
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
    public boolean changeState(UUID taskId, TaskState state) {
        return this.updateField(taskId.toString(), ".state", state);
    }

    /**
     * Update assigned to boolean.
     *
     * @param taskId     the task id
     * @param assignedTo the assigned to
     * @return the boolean
     */
    public boolean updateAssignedTo(UUID taskId, UUID assignedTo) {
        return this.updateField(taskId.toString(), ".assignedTo", assignedTo);
    }

    public boolean updateChannelSession(UUID taskId, ChannelSession channelSession) {
        return this.updateField(taskId.toString(), ".channelSession", channelSession);
    }
}
