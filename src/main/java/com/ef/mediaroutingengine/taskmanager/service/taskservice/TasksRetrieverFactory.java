package com.ef.mediaroutingengine.taskmanager.service.taskservice;

import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Task retriever factory.
 */
@Service
public class TasksRetrieverFactory {
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;

    /**
     * Instantiates a new tasks' retriever factory.
     *
     * @param tasksPool the tasks pool
     */
    @Autowired
    public TasksRetrieverFactory(TasksPool tasksPool) {
        this.tasksPool = tasksPool;
    }

    /**
     * Gets retriever.
     *
     * @param agentId   the agent id
     * @param stateName the state name
     * @return the retriever
     */
    public TasksRetriever getRetriever(Optional<String> agentId, Optional<Enums.TaskStateName> stateName) {
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
