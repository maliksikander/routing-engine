package com.ef.mediaroutingengine.services;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.TaskService;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentsPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentsPool.class);

    private final AgentsRepository agentsRepository;
    private final Map<UUID, Agent> agents;

    @Autowired
    public AgentsPool(AgentsRepository agentsRepository) {
        this.agentsRepository = agentsRepository;
        agents = new ConcurrentHashMap<>();
    }

    /**
     * Loads All agents from DB into Agents pool in ram.
     */
    public void loadFromDb() {
        this.agents.clear();
        List<CCUser> ccUsers = agentsRepository.findAll();
        for (CCUser ccUser: ccUsers) {
            this.agents.put(ccUser.getId(), new Agent(ccUser));
        }
    }

    /**
     * Finds an agent by it's keycloak user id, which is basically the agent's overall id as well.
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
     * Ends the task assigned to a particular agent in the pool.
     *
     * @param task the task to end
     * @return true if found and ended, false otherwise
     */
    public boolean endTask(TaskService task) {
        Agent assignedTo = this.agents.get(task.getAssignedTo());
        if (assignedTo != null) {
            assignedTo.endTask(task);
            return true;
        }
        LOGGER.warn("The agent: {} assigned to task: {} not found in Agents pool",
                task.getAssignedTo(), task.getId());
        return false;
    }

    /**
     * Logs all the agents in the agents pool.
     */
    public void logAll() {
        LOGGER.info("LOGGING ALL AGENTS IN THE AGENT POOL");
        LOGGER.info("------------------------------------");
        if (this.agents.isEmpty()) {
            LOGGER.info("Agents pool is empty");
        } else {
            this.agents.forEach((k, v) -> LOGGER.info("Agent: {} | {}", k, v));
        }
        LOGGER.info("------------------------------------");
    }

    /**
     * Converts the Agents pool into an ArrayList collection.
     *
     * @return ArrayList of Agents
     */
    public List<Agent> toList() {
        List<Agent> agentList = new ArrayList<>();
        this.agents.forEach((k, v) -> agentList.add(v));
        return agentList;
    }
}
