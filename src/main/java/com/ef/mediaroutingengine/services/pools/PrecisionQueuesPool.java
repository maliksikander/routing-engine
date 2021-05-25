package com.ef.mediaroutingengine.services.pools;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.Expression;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Step;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.Term;
import com.ef.mediaroutingengine.repositories.PrecisionQueueEntityRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PrecisionQueuesPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrecisionQueuesPool.class);

    private final AgentsPool agentsPool;
    private final PrecisionQueueEntityRepository pqRepository;

    private final Map<UUID, PrecisionQueue> precisionQueues;

    /**
     * Constructor. Autowired, loads the beans.
     *
     * @param agentsPool   all agents
     * @param pqRepository to communicate with PrecisionQueues collection in DB
     */
    @Autowired
    public PrecisionQueuesPool(AgentsPool agentsPool, PrecisionQueueEntityRepository pqRepository) {
        this.agentsPool = agentsPool;
        this.pqRepository = pqRepository;
        this.precisionQueues = new ConcurrentHashMap<>();
    }

    /**
     * Loads all the precision queues from DB.
     */
    public void loadAllFromDb() {
        List<PrecisionQueueEntity> precisionQueueEntities = this.pqRepository.findAll();
        List<Agent> allAgents = agentsPool.toList();
        if (allAgents.isEmpty()) {
            LOGGER.warn("Agents pool is empty");
        }

        for (PrecisionQueueEntity entity : precisionQueueEntities) {
            PrecisionQueue precisionQueue = new PrecisionQueue(entity);
            precisionQueue.evaluateAgentsAssociatedWithSteps(allAgents);
            this.precisionQueues.put(precisionQueue.getId(), precisionQueue);
        }
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

    /**
     * Evaluates the agents associated with steps in a single precision-queue.
     *
     * @param entity precision-queue entity in the DB
     */
    public void evaluateAssociatedAgents(PrecisionQueueEntity entity) {
        List<Agent> allAgents = agentsPool.toList();
        if (allAgents.isEmpty()) {
            LOGGER.warn("Agents pool is empty");
        }

        PrecisionQueue precisionQueue = new PrecisionQueue(entity);
        precisionQueue.evaluateAgentsAssociatedWithSteps(allAgents);
        this.precisionQueues.put(precisionQueue.getId(), precisionQueue);
    }

    /**
     * Evaluates the agents associated with steps of all precision-queues.
     */
    public void evaluateAssociatedAgentsForAll() {
        List<Agent> agents = this.agentsPool.toList();
        List<PrecisionQueueEntity> pqEntities = pqRepository.findAll();

        for (PrecisionQueueEntity entity : pqEntities) {
            PrecisionQueue precisionQueue = new PrecisionQueue(entity);
            precisionQueue.evaluateAgentsAssociatedWithSteps(agents);
            precisionQueues.put(precisionQueue.getId(), precisionQueue);
        }
    }

    /**
     * Updates the routing-attribute in all precision-queues.
     *
     * @param updated the routing attribute to be updated
     */
    public void updateRoutingAttribute(RoutingAttribute updated) {
        for (Map.Entry<UUID, PrecisionQueue> entry : this.precisionQueues.entrySet()) {
            PrecisionQueue precisionQueue = entry.getValue();
            for (Step step : precisionQueue.getSteps()) {
                for (Expression expression : step.getExpressions()) {
                    for (Term term : expression.getTerms()) {
                        RoutingAttribute routingAttribute = term.getRoutingAttribute();
                        if (updated.equals(routingAttribute)) {
                            routingAttribute.setName(updated.getName());
                            routingAttribute.setDescription(updated.getDescription());
                            routingAttribute.setDefaultValue(updated.getDefaultValue());
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates Media routing domain object in all precision-queues.
     *
     * @param updated mrd to be updated
     */
    public void updateMediaRoutingDomain(MediaRoutingDomain updated) {
        for (Map.Entry<UUID, PrecisionQueue> entry : this.precisionQueues.entrySet()) {
            PrecisionQueue precisionQueue = entry.getValue();
            MediaRoutingDomain mrd = precisionQueue.getMrd();
            if (mrd.equals(updated)) {
                mrd.setName(updated.getName());
                mrd.setDescription(updated.getDescription());
                mrd.setInterruptible(updated.isInterruptible());
            }
        }
    }

    /**
     * Removes agent from all precision-queues.
     *
     * @param id unique id of agent to be removed
     */
    public void removeAgentFromAll(UUID id) {
        for (Map.Entry<UUID, PrecisionQueue> entry : this.precisionQueues.entrySet()) {
            PrecisionQueue precisionQueue = entry.getValue();
            for (Step step : precisionQueue.getSteps()) {
                step.removeAssociatedAgent(id);
            }
        }
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
            queue.endTask(task.getId().toString());
            return true;
        }
        return false;
    }
}
