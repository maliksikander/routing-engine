package com.ef.mediaroutingengine.routing.pool;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.routing.model.Agent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * The type Agents pool.
 */
@Service
public class AgentsPool {
    /**
     * The Agents.
     */
    private final Map<String, Agent> pool = new ConcurrentHashMap<>();

    /**
     * Loads the pool at start of the application.
     *
     * @param ccUsers list of CCUsers from the config DB.
     */
    public void loadFrom(List<CCUser> ccUsers) {
        this.pool.clear();
        ccUsers.forEach(ccUser -> this.pool.put(ccUser.getId(), new Agent(ccUser)));
    }

    /**
     * Insert.
     *
     * @param agent the agent
     */
    public void insert(Agent agent) {
        if (agent != null) {
            this.pool.putIfAbsent(agent.getId(), agent);
        }
    }

    /**
     * Finds an agent by its keycloak user id, which is basically the agent's overall id as well.
     *
     * @param id find the agent by this id.
     * @return Agent if found, null otherwise.
     */
    public Agent findById(String id) {
        if (id == null) {
            return null;
        }
        return pool.get(id);
    }

    /**
     * Converts the Agents pool into an ArrayList collection.
     *
     * @return ArrayList of Agents
     */
    public List<Agent> findAll() {
        List<Agent> agentList = new ArrayList<>();
        this.pool.forEach((k, v) -> agentList.add(v));
        return agentList;
    }

    /**
     * Delete by id.
     *
     * @param id the id
     */
    public void deleteById(String id) {
        if (id != null) {
            this.pool.remove(id);
        }
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return this.pool.size();
    }
}
