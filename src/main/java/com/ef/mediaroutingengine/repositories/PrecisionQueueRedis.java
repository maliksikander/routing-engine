package com.ef.mediaroutingengine.repositories;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.model.Expression;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Step;
import com.ef.mediaroutingengine.model.Term;
import com.ef.mediaroutingengine.services.redis.RedisClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PrecisionQueueRedis {
    private final RedisClient redisClient;
    private final AgentsRepository agentsRepository;
    private final PrecisionQueueEntityRepository pqRepository;
    private static final String REDIS_KEY = "activePrecisionQueues";

    /**
     * Constructor. Autowired, loads the beans.
     *
     * @param redisClient for communication with redis server
     * @param agentsRepository to communicate with Agents collection in DB
     * @param pqRepository to communicate with PrecisionQueues collection in DB
     */
    @Autowired
    public PrecisionQueueRedis(RedisClient redisClient,
                               AgentsRepository agentsRepository,
                               PrecisionQueueEntityRepository pqRepository) {
        this.redisClient = redisClient;
        this.agentsRepository = agentsRepository;
        this.pqRepository = pqRepository;
    }

    public void add(PrecisionQueueEntity entity) throws Exception {
        this.update((List<PrecisionQueue> precisionQueues) -> precisionQueues.add(new PrecisionQueue(entity)));
    }

    public void add(PrecisionQueue precisionQueue) throws Exception {
        this.update((List<PrecisionQueue> precisionQueues) -> precisionQueues.add(precisionQueue));
    }

    /**
     * Removes precision-queue from the redis-cache if id found.
     *
     * @param id of precision-queue to be removed.
     * @throws Exception If there is issue in connecting with redis server or serialization.
     */
    public void remove(UUID id) throws Exception {
        this.update((List<PrecisionQueue> precisionQueues) -> {
            int index = -1;
            for (int i = 0; i < precisionQueues.size(); i++) {
                if (precisionQueues.get(i).getId().equals(id)) {
                    index = i;
                    break;
                }
            }
            if (index > -1) {
                precisionQueues.remove(index);
            }
        });
    }

    /**
     *  Evaluates the agents associated with steps in a single precision-queue.
     *
     * @param entity precision-queue entity in the DB
     */
    public void evaluateAssociatedAgents(PrecisionQueueEntity entity) {
        List<CCUser> agents = agentsRepository.findAll();
        try {
            List<PrecisionQueue> precisionQueues = redisClient.getJsonArray(REDIS_KEY, PrecisionQueue.class);
            for (int i = 0; i < precisionQueues.size(); i++) {
                if (precisionQueues.get(i).getId().equals(entity.getId())) {
                    precisionQueues.set(i, new PrecisionQueue(entity));
                    precisionQueues.get(i).evaluateAgentsAssociatedWithSteps(agents);
                    redisClient.setJSON(REDIS_KEY, precisionQueues);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Evaluates the agents associated with steps of all precision-queues.
     *
     * @throws Exception If there is issue in connecting with redis server or serialization.
     */
    public void evaluateAssociatedAgentsForAll() throws Exception {
        List<CCUser> agents = agentsRepository.findAll();
        List<PrecisionQueueEntity> pqEntities = pqRepository.findAll();

        List<PrecisionQueue> precisionQueues = new ArrayList<>();
        for (PrecisionQueueEntity entity : pqEntities) {
            PrecisionQueue precisionQueue = new PrecisionQueue(entity);
            precisionQueue.evaluateAgentsAssociatedWithSteps(agents);
            precisionQueues.add(precisionQueue);
        }
        this.redisClient.setJSON(REDIS_KEY, precisionQueues);
    }

    /**
     * Updates the routing-attribute in all precision-queues.
     *
     * @param updated the routing attribute to be updated
     * @throws Exception If there is issue in connecting with redis server or serialization.
     */
    public void updateRoutingAttribute(RoutingAttribute updated) throws Exception {
        this.update((List<PrecisionQueue> precisionQueues) -> {
            for (PrecisionQueue precisionQueue : precisionQueues) {
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
                    for (CCUser agent: step.getAssociatedAgents()) {
                        for (AssociatedRoutingAttribute element: agent.getAssociatedRoutingAttributes()) {
                            RoutingAttribute routingAttribute = element.getRoutingAttribute();
                            if (updated.equals(routingAttribute)) {
                                routingAttribute.setName(updated.getName());
                                routingAttribute.setDescription(updated.getDescription());
                                routingAttribute.setDefaultValue(updated.getDefaultValue());
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Updates Media routing domain object in all precision-queues.
     *
     * @param updated mrd to be updated
     * @throws Exception If there is issue in connecting with redis server or serialization.
     */
    public void updateMediaRoutingDomain(MediaRoutingDomain updated) throws Exception {
        this.update((List<PrecisionQueue> precisionQueues) -> {
            for (PrecisionQueue precisionQueue : precisionQueues) {
                MediaRoutingDomain mrd = precisionQueue.getMrd();
                if (mrd.equals(updated)) {
                    mrd.setName(updated.getName());
                    mrd.setDescription(updated.getDescription());
                    mrd.setInterruptible(updated.isInterruptible());
                }
            }
        });
    }

    /**
     * Removes agent from all precision-queues.
     *
     * @param id unique id of agent to be removed
     * @throws Exception If there is issue in connecting with redis server or serialization.
     */
    public void removeAgentFromAll(UUID id) throws Exception {
        this.update((List<PrecisionQueue> precisionQueues) -> {
            for (PrecisionQueue precisionQueue : precisionQueues) {
                for (Step step : precisionQueue.getSteps()) {
                    int index = -1;
                    List<CCUser> associatedAgents = step.getAssociatedAgents();
                    for (int i = 0; i < associatedAgents.size(); i++) {
                        CCUser agent = associatedAgents.get(i);
                        if (agent.getId().equals(id)) {
                            index = i;
                            break;
                        }
                    }
                    if (index > -1) {
                        associatedAgents.remove(index);
                    }
                }
            }
        });
    }

    public boolean collectionExists() throws JsonProcessingException {
        return redisClient.getJSON(REDIS_KEY, ArrayList.class) != null;
    }

    private void update(Consumer<List<PrecisionQueue>> function) throws Exception {
        List<PrecisionQueue> precisionQueues = redisClient.getJsonArray(REDIS_KEY, PrecisionQueue.class);
        function.accept(precisionQueues);
        redisClient.setJSON(REDIS_KEY, precisionQueues);
    }
}
