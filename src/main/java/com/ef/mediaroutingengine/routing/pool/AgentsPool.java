package com.ef.mediaroutingengine.routing.pool;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The type Agents pool.
 */
@Service
public class AgentsPool {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(AgentsPool.class);
    /**
     * The Agents.
     */
    private final Map<UUID, Agent> agents = new ConcurrentHashMap<>();

    /**
     * Loads the pool at start of the application.
     *
     * @param ccUsers list of CCUsers from the config DB.
     */
    public void loadPoolFrom(List<CCUser> ccUsers) {
        this.agents.clear();
        for (CCUser ccUser : ccUsers) {
            this.agents.put(ccUser.getId(), new Agent(ccUser));
        }
    }

    /**
     * Insert.
     *
     * @param agent the agent
     */
    public void insert(Agent agent) {
        if (agent != null) {
            this.agents.putIfAbsent(agent.getId(), agent);
        }
    }

    /**
     * Finds an agent by its keycloak user id, which is basically the agent's overall id as well.
     *
     * @param id find the agent by this id.
     * @return Agent if found, null otherwise.
     */
    public Agent findById(UUID id) {
        if (id == null) {
            return null;
        }
        return agents.get(id);
    }

    /**
     * Converts the Agents pool into an ArrayList collection.
     *
     * @return ArrayList of Agents
     */
    public List<Agent> findAll() {
        List<Agent> agentList = new ArrayList<>();
        this.agents.forEach((k, v) -> agentList.add(v));
        return agentList;
    }

    /**
     * Delete by id.
     *
     * @param id the id
     */
    public void deleteById(UUID id) {
        if (id != null) {
            this.agents.remove(id);
        }
    }

    /**
     * Ends the task assigned to a particular agent in the pool.
     *
     * @param task the task to end
     * @return true if found and ended, false otherwise
     */
    public boolean endTask(Task task) {
        Agent assignedTo = this.agents.get(task.getAssignedTo());
        if (assignedTo != null) {
            assignedTo.removeTask(task);
            return true;
        }
        logger.warn("The agent: {} assigned to task: {} not found in Agents pool",
                task.getAssignedTo(), task.getId());
        return false;
    }

    /**
     * Logs all the agents in the agents pool.
     */
    public void logAll() {
        logger.info("LOGGING ALL AGENTS IN THE AGENT POOL");
        logger.info("------------------------------------");
        if (this.agents.isEmpty()) {
            logger.info("Agents pool is empty");
        } else {
            this.agents.forEach((k, v) -> logger.info("Agent: {} | {}", k, v));
        }
        logger.info("------------------------------------");
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return this.agents.size();
    }
}
