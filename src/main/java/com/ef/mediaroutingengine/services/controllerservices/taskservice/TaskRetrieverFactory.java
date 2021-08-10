package com.ef.mediaroutingengine.services.controllerservices.taskservice;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Task retriever factory.
 */
@Service
public class TaskRetrieverFactory {
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;

    /**
     * Instantiates a new Task retriever factory.
     *
     * @param tasksPool the tasks pool
     */
    @Autowired
    public TaskRetrieverFactory(TasksPool tasksPool) {
        this.tasksPool = tasksPool;
    }

    /**
     * Gets retriever.
     *
     * @param agentId   the agent id
     * @param stateName the state name
     * @return the retriever
     */
    public TasksRetriever getRetriever(Optional<UUID> agentId, Optional<Enums.TaskStateName> stateName) {
        if (agentId.isPresent() && stateName.isPresent()) {
            return new RetrieveByAgentAndState(this.tasksPool, agentId.get(), stateName.get());
        } else if (agentId.isPresent()) {
            return new RetrieveByAgent(this.tasksPool, agentId.get());
        } else if (stateName.isPresent()) {
            return new RetrieveByState(this.tasksPool, stateName.get());
        } else {
            return new RetrieveAll(this.tasksPool);
        }
    }
}
