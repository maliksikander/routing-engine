package com.ef.mediaroutingengine.routing.pool;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.routing.TaskRouter;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.NewTaskPayload;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.model.QueueEventName;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.MDC;
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
     * @param entities   the precision queue entities
     * @param agentsPool the agents pool
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

    /**
     * Publish new request.
     *
     * @param task  the task
     * @param media the media
     */
    public void publishNewRequest(Task task, TaskMedia media) {
        String evtName = QueueEventName.NEW_REQUEST;
        NewTaskPayload payload = new NewTaskPayload(task, media);
        this.notifyQueues(new PropertyChangeEvent(this, evtName, null, payload));
    }

    /**
     * Publish agent available.
     *
     * @param agentMrdState the agent mrd state
     */
    public void publishAgentAvailable(AgentMrdState agentMrdState) {
        String eventName = QueueEventName.AGENT_AVAILABLE;
        PropertyChangeEvent evt = new PropertyChangeEvent(this, eventName, null, agentMrdState);
        this.notifyQueues(agentMrdState.getMrd().getId(), evt);
    }

    /**
     * Publish request accepted.
     *
     * @param mrdId the mrd id
     */
    public void publishRequestAccepted(String mrdId) {
        String eventName = QueueEventName.REQUEST_ACCEPTED;
        this.notifyQueues(mrdId, new PropertyChangeEvent(this, eventName, null, null));
    }

    /**
     * Publish on failover.
     */
    public void publishOnFailover() {
        this.notifyQueues(new PropertyChangeEvent(this, QueueEventName.ON_FAILOVER, null, null));
    }

    /**
     * Notify queues.
     *
     * @param mrdId the mrd id
     * @param evt   the evt
     */
    private void notifyQueues(String mrdId, PropertyChangeEvent evt) {
        this.notifyQueues(evt, this.toList().stream().filter(p -> p.getMrd().getId().equals(mrdId)).toList());
    }

    /**
     * Notify queues.
     *
     * @param evt the evt
     */
    private void notifyQueues(PropertyChangeEvent evt) {
        this.notifyQueues(evt, this.toList());
    }

    /**
     * Notify queues.
     *
     * @param evt    the evt
     * @param queues the queues
     */
    private void notifyQueues(PropertyChangeEvent evt, List<PrecisionQueue> queues) {
        String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);
        CompletableFuture.runAsync(() -> {
            MDC.put(Constants.MDC_CORRELATION_ID, correlationId);
            for (PrecisionQueue queue : queues) {
                queue.getTaskRouter().propertyChange(evt);
            }
            MDC.clear();
        });
    }
}
