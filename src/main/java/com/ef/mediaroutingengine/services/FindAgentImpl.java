package com.ef.mediaroutingengine.services;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.services.redis.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FindAgentImpl implements FindAgent {

    private final RedisClient redisClient;

    @Autowired
    public FindAgentImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    /**
     * finds agent.
     *
     * @param flag boolean
     * @return CCUser
     */
    public CCUser find(boolean flag) {
        if (flag) {
            try {
                String key = "agentPresence:8d42617c-0603-4fbe-9863-2507c0fff9fd";
                return this.redisClient.getJson(key, AgentPresence.class).getAgent();
            } catch (Exception e) {
                System.out.println("find agent exception starts");
                System.out.println(e.getMessage());
                System.out.println("find agent exception ends");
            }
        }
        return null;
    }
}
