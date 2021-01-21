package com.ef.mediaroutingengine.services;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.Constants.GeneralConstants;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.redis.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FindAgentImpl implements FindAgent{
    private final RedisClient redisClient;

    @Autowired
    public FindAgentImpl(RedisClient redisClient){
        this.redisClient = redisClient;
    }

    public CCUser find(boolean flag){
        if(flag) {
            try {
                this.redisClient.getJSON(GeneralConstants.agentPresenceKey, AgentPresence.class);
            } catch (Exception e){
                System.out.println();
            }
        }
        return null;
    }
}
