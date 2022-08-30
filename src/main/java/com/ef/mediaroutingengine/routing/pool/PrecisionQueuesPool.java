package com.ef.mediaroutingengine.routing.pool;

import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.mediaroutingengine.routing.TaskRouter;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Repository;

/**
 * The type Precision queues pool.
 */
@Repository
public class PrecisionQueuesPool {
    /**
     * The Precision queues.
     */
    private final Map<String, PrecisionQueue> pool = new ConcurrentHashMap<>();

    /**
     * Loads all the precision queues from DB.
     *
     * @param entities the precision queue entities
     * @param agentsPool             the agents pool
     */
    public void loadFrom(List<PrecisionQueueEntity> entities, AgentsPool agentsPool) {
        this.pool.clear();
        entities.forEach(e -> this.pool.put(e.getId(), new PrecisionQueue(e, agentsPool, getTaskSchedulerBean())));
    }

    /**
     * Gets task scheduler bean.
     *
     * @return the task scheduler bean
     */
    @Lookup
    public TaskRouter getTaskSchedulerBean() {
        return null;
    }

    /**
     * Insert.
     *
     * @param precisionQueue the precision queue
     */
    public void insert(PrecisionQueue precisionQueue) {
        this.pool.putIfAbsent(precisionQueue.getId(), precisionQueue);
    }

    /**
     * Return PrecisionQueue with the id in the parameter. Return null if id not found or id is null.
     *
     * @param id Precision Queue with this id will be searched and returned.
     * @return Precision Queue if found, null otherwise
     */
    public PrecisionQueue findById(String id) {
        if (id == null) {
            return null;
        }
        return this.pool.get(id);
    }

    /**
     * Find by name precision queue.
     *
     * @param name the name
     * @return the precision queue
     */
    public PrecisionQueue findByName(String name) {
        for (PrecisionQueue queue : this.pool.values()) {
            if (queue.getName().equals(name)) {
                return queue;
            }
        }
        return null;
    }

    /**
     * Removes precision-queue from the redis-cache if id found.
     *
     * @param id of precision-queue to be removed.
     * @return true if removed
     */
    public boolean deleteById(String id) {
        PrecisionQueue removed = this.pool.remove(id);
        return removed != null;
    }

    /**
     * Calculate avg talk time of long.
     *
     * @param queue the queue
     * @param task  the task
     * @return the long
     */
    private long calculateAvgTalkTimeOf(PrecisionQueue queue, Task task) {
        long currentTotalTalkTime = queue.getAverageTalkTime() * queue.getNoOfTask();
        long newTotalTalkTime = currentTotalTalkTime + task.getHandlingTime();
        return newTotalTalkTime / (queue.getNoOfTask() + 1);
    }

    /**
     * Ends a task in the particular precision queue in the pool.
     *
     * @param task the task to end
     * @return true if task found and ended, false otherwise
     */
    public boolean endTask(Task task) {
        String queueId = task.getQueue();
        if (queueId == null) {
            return false;
        }
        PrecisionQueue queue = this.pool.get(queueId);
        if (queue != null) {
            if (queue.getAverageTalkTime() != null && queue.getAverageTalkTime() > 0) {
                queue.setAverageTalkTime(calculateAvgTalkTimeOf(queue, task));
            } else {
                queue.setAverageTalkTime(task.getHandlingTime());
            }
            queue.incrNoOfTask();
            queue.removeTask(task);
            return true;
        }
        return false;
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return this.pool.size();
    }

    /**
     * Converts the pool to list of all Precision queues.
     *
     * @return list of all precision queues.
     */
    public List<PrecisionQueue> toList() {
        List<PrecisionQueue> precisionQueueList = new ArrayList<>();
        for (Map.Entry<String, PrecisionQueue> entry : this.pool.entrySet()) {
            precisionQueueList.add(entry.getValue());
        }
        return precisionQueueList;
    }

    /**
     * Evaluate on insert for all.
     *
     * @param agent the agent
     */
    public void evaluateOnInsertForAll(Agent agent) {
        pool.forEach((k, v) -> v.evaluateAssociatedAgentOnInsert(agent));
    }

    /**
     * Evaluate on update for all.
     *
     * @param agent the agent
     */
    public void evaluateOnUpdateForAll(Agent agent) {
        pool.forEach((k, v) -> v.evaluateAssociatedAgentOnUpdate(agent));
    }

    /**
     * Delete from a ll.
     *
     * @param agent the agent
     */
    public void deleteFromAll(Agent agent) {
        pool.forEach((k, v) -> v.deleteAssociatedAgentFromAll(agent));
    }
}
