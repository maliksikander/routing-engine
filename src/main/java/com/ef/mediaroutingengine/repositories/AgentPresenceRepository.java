package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.services.redis.RedisClient;
import com.ef.mediaroutingengine.services.redis.RedisJsonDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgentPresenceRepository extends RedisJsonDao<AgentPresence> {

    @Autowired
    public AgentPresenceRepository(RedisClient redisClient) {
        super(redisClient, "agentPresence");
    }
}