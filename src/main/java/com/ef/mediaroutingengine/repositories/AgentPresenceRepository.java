package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.services.redis.RedisClient;
import com.ef.mediaroutingengine.services.redis.RedisJsonDao;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
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
    public boolean updateAgentState(UUID agentId, AgentState agentState) {
        String id = agentId.toString();
        this.updateField(id, ".state", agentState);
        return this.updateField(id, ".stateChangeTime", new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Update agent mrd state list boolean.
     *
     * @param agentId        the agent id
     * @param agentMrdStates the agent mrd states
     * @return the boolean
     */
    public boolean updateAgentMrdStateList(UUID agentId, List<AgentMrdState> agentMrdStates) {
        return this.updateField(agentId.toString(), ".agentMrdStates", agentMrdStates);
    }
}