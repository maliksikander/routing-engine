package com.ef.mediaroutingengine.agentstatemanager.repository;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.global.redis.RedisClient;
import com.ef.mediaroutingengine.global.redis.RedisJsonDao;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The type Agent presence repository.
 */
@Component
public class AgentPresenceRepository extends RedisJsonDao<AgentPresence> {

    /**
     * Instantiates a new Agent presence repository.
     *
     * @param redisClient the redis client
     */
    @Autowired
    public AgentPresenceRepository(RedisClient redisClient) {
        super(redisClient, "agentPresence");
    }

    /**
     * Updates the Agent state in the Repository.
     *
     * @param agentId    id of the agent.
     * @param agentState Agent-state to be updated
     * @return true if state change successful, false otherwise
     */
    public boolean updateAgentState(String agentId, AgentState agentState) {
        this.updateField(agentId, ".state", agentState);
        return this.updateField(agentId, ".stateChangeTime", new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Update agent mrd state list boolean.
     *
     * @param agentId        the agent id
     * @param agentMrdStates the agent mrd states
     * @return the boolean
     */
    public boolean updateAgentMrdStateList(String agentId, List<AgentMrdState> agentMrdStates) {
        return this.updateField(agentId, ".agentMrdStates", agentMrdStates);
    }

    /**
     * Update cc user boolean.
     *
     * @param ccUser the cc user
     * @return the boolean
     */
    public boolean updateCcUser(CCUser ccUser) {
        return this.updateField(ccUser.getId(), ".agent", ccUser);
    }
}