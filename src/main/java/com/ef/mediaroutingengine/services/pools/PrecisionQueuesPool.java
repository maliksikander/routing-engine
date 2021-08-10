package com.ef.mediaroutingengine.services.pools;

import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.TaskScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Repository;

/**
 * The type Precision queues pool.
 */
@Repository
public class PrecisionQueuesPool {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PrecisionQueuesPool.class);
    /**
     * The Precision queues.
     */
    private final Map<UUID, PrecisionQueue> precisionQueues = new ConcurrentHashMap<>();

    /**
     * Loads all the precision queues from DB.
     *
     * @param precisionQueueEntities the precision queue entities
     * @param agentsPool             the agents pool
     */
    public void loadPoolFrom(List<PrecisionQueueEntity> precisionQueueEntities, AgentsPool agentsPool) {
        for (PrecisionQueueEntity entity : precisionQueueEntities) {
            PrecisionQueue precisionQueue = new PrecisionQueue(entity, agentsPool, getTaskSchedulerBean());
            this.precisionQueues.put(precisionQueue.getId(), precisionQueue);
        }
    }

    /**
     * Gets task scheduler bean.
     *
     * @return the task scheduler bean
     */
    @Lookup
    public TaskScheduler getTaskSchedulerBean() {
        return null;
    }

    /**
     * Finds a precision queue by name.
     *
     * @param name name of the queue to find
     * @return PrecisionQueue object if found, null otherwise
     */
    public PrecisionQueue findByName(String name) {
        for (Map.Entry<UUID, PrecisionQueue> entry : this.precisionQueues.entrySet()) {
            if (entry.getValue().getName().equals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Return PrecisionQueue with the id in the parameter. Return null if id not found or id is null.
     *
     * @param id Precision Queue with this id will be searched and returned.
     * @return Precision Queue if found, null otherwise
     */
    public PrecisionQueue findById(UUID id) {
        if (id == null) {
            return null;
        }
        return this.precisionQueues.get(id);
    }

    /**
     * Removes precision-queue from the redis-cache if id found.
     *
     * @param id of precision-queue to be removed.
     * @return true if removed
     */
    public boolean remove(UUID id) {
        PrecisionQueue removed = this.precisionQueues.remove(id);
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
        PrecisionQueue queue = findByName(task.getQueue().toString());
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
        return this.precisionQueues.size();
    }

    /**
     * Converts the pool to list of all Precision queues.
     *
     * @return list of all precision queues.
     */
    public List<PrecisionQueue> toList() {
        List<PrecisionQueue> precisionQueueList = new ArrayList<>();
        for (Map.Entry<UUID, PrecisionQueue> entry : this.precisionQueues.entrySet()) {
            precisionQueueList.add(entry.getValue());
        }
        return precisionQueueList;
    }
}