package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.dto.RedisEvent;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.services.redis.ChannelPublish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class MockRedisPubSub {
    private final ChannelPublish channelPublish;

    @Autowired
    public MockRedisPubSub(ChannelPublish channelPublish) {
        this.channelPublish = channelPublish;
    }

    /**
     * Mocks the behavior of publishing a message on the redis pub sub.
     *
     * @return Response Entity.
     */
    @GetMapping("/redis-publish")
    public ResponseEntity<Object> redisPublish() {
        TaskDto taskDto = new TaskDto();
        taskDto.setState(Enums.TaskState.CREATED);

        RedisEvent redisEvent = new RedisEvent();
        redisEvent.setName(Enums.RedisEventName.TASK_STATE_CHANGED);
        redisEvent.setData(taskDto);

        this.channelPublish.publish(redisEvent);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
