package com.ef.mediaroutingengine.services.redis;

import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Enums;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskDao extends RedisJsonDao<TaskDto> {

    @Autowired
    public TaskDao(RedisClient redisClient) {
        super(redisClient, "task");
    }

    public boolean changeState(UUID taskId, Enums.TaskState taskState) {
        String path = ".state";
        return this.updateField(taskId.toString(), path, taskState);
    }
}
