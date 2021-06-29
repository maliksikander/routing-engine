package com.ef.mediaroutingengine.services.pools;

import com.ef.mediaroutingengine.commons.Enums;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Repository;

@Repository
public class PrecisionQueuesPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrecisionQueuesPool.class);

    private final AgentsPool agentsPool;
    private final Map<UUID, PrecisionQueue> precisionQueues;

    /**
     * Constructor. Autowired, loads the beans.
     *
     * @param agentsPool all agents
     */
    @Autowired
    public PrecisionQueuesPool(AgentsPool agentsPool) {
        this.agentsPool = agentsPool;
        this.precisionQueues = new ConcurrentHashMap<>();
    }

    /**
     * Loads all the precision queues from DB.
     */
    public void loadPoolFrom(List<PrecisionQueueEntity> precisionQueueEntities) {
        for (PrecisionQueueEntity entity : precisionQueueEntities) {
            PrecisionQueue precisionQueue = new PrecisionQueue(entity, this.agentsPool, getTaskSchedulerBean());
            this.precisionQueues.put(precisionQueue.getId(), precisionQueue);
        }
    }

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

    public PrecisionQueue findById(UUID id) {
        return this.precisionQueues.get(id);
    }

    public PrecisionQueue getDefaultQueue() {
        return this.findByName(Enums.DefaultQueue.DEFAULT_PRECISION_QUEUE.name());
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
