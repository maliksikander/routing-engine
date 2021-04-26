package com.ef.mediaroutingengine.services.redispubsub;

import com.ef.mediaroutingengine.dto.RedisEvent;

public interface MessagePublisher {
    void publish(RedisEvent message);
}