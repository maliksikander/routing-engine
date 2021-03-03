package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.model.Task;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class RoutingEngineCache {

    private final Map<String, Task> taskCache = new ConcurrentHashMap<>();

    public boolean taskExists(String topicId) {
        return this.taskCache.containsKey(topicId);
    }

    public void addTask(String topicId, Task task) {
        this.taskCache.put(topicId, task);
    }

    /**
     * Removes task from cache.
     *
     * @param topicId String
     */
    public void removeTask(String topicId) {
        if (this.taskExists(topicId)) {
            this.taskCache.remove(topicId);
        }
    }

    /**
     * Changes task state in cache.
     *
     * @param topicId String
     * @param state String
     */
    public void changeTaskState(String topicId, String state) {
        if (this.taskExists(topicId)) {
            this.taskCache.get(topicId).setState(state);
        }
    }
}
