package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Enums;
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

    public boolean changeState(UUID taskId, Enums.TaskStateName taskStateName) {
        String path = ".state";
        return this.updateField(taskId.toString(), path, taskStateName);
    }
}
