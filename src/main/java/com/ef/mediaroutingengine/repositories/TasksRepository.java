package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.redis.RedisClient;
import com.ef.mediaroutingengine.services.redis.RedisJsonDao;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TasksRepository extends RedisJsonDao<TaskDto> {

    @Autowired
    public TasksRepository(RedisClient redisClient) {
        super(redisClient, "task");
    }

    public boolean changeState(UUID taskId, TaskState state) {
        return this.updateField(taskId.toString(), ".state", state);
    }

    public boolean updateAssignedTo(UUID taskId, UUID assignedTo) {
        return this.updateField(taskId.toString(), ".assignedTo", assignedTo);
    }
}
