package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ExpressionEntity;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.StepEntity;
import com.ef.cim.objectmodel.TermEntity;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.dto.RoutingAttributeDeleteConflictResponse;
import com.ef.mediaroutingengine.routing.pool.RoutingAttributesPool;
import com.ef.mediaroutingengine.routing.repository.AgentsRepository;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.routing.repository.RoutingAttributeRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Routing attributes service.
 */
@Service
public class RoutingAttributesServiceImpl implements RoutingAttributesService {
    private final Logger logger = LoggerFactory.getLogger(RoutingAttributesServiceImpl.class);
    /**
     * The Repository.
     */
    private final RoutingAttributeRepository repository;
    /**
     * The Routing attributes pool.
     */
    private final RoutingAttributesPool routingAttributesPool;
    /**
     * The Precision queue entity repository.
     */
    private final PrecisionQueueRepository precisionQueueRepository;
    /**
     * The Agents repository.
     */
    private final AgentsRepository agentsRepository;

    /**
     * Default constructor.
     *
     * @param repository               routing attribute repository
     * @param routingAttributesPool    the routing attributes pool
     * @param precisionQueueRepository precision queue repository
     * @param agentsRepository         agents repository
     */
    @Autowired
    public RoutingAttributesServiceImpl(RoutingAttributeRepository repository,
                                        RoutingAttributesPool routingAttributesPool,
                                        PrecisionQueueRepository precisionQueueRepository,
                                        AgentsRepository agentsRepository) {
        this.repository = repository;
        this.routingAttributesPool = routingAttributesPool;
        this.precisionQueueRepository = precisionQueueRepository;
        this.agentsRepository = agentsRepository;
    }

    @Override
    public RoutingAttribute create(RoutingAttribute routingAttribute) {
        logger.info("Create RoutingAttribute request initiated");

        RoutingAttribute inserted = repository.insert(routingAttribute);
        logger.debug("RoutingAttribute inserted in RoutingAttribute Config DB | Attribute: {}", inserted.getId());

        this.routingAttributesPool.insert(inserted);
        logger.debug("RoutingAttribute inserted in RoutingAttributes Pool | Attribute: {}", inserted.getId());

        logger.info("RoutingAttribute created successfully | Attribute: {}", inserted.getId());
        return inserted;
    }

    @Override
    public List<RoutingAttribute> retrieve() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public RoutingAttribute update(RoutingAttribute routingAttribute, String id) {
        logger.info("Update RoutingAttribute request initiated with id: {}", id);

        if (!this.repository.existsById(id)) {
            String errorMessage = "Could not find RoutingAttribute: " + id + " resource to update";
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        routingAttribute.setId(id);

        this.updatePrecisionQueues(routingAttribute, id);
        logger.debug("RoutingAttribute {} updated in PrecisionQueues in PQ-Config-DB", id);

        this.updateAgents(routingAttribute, id);
        logger.debug("RoutingAttribute {} updated in Agents in Agents-Config-DB", id);

        this.routingAttributesPool.update(routingAttribute);
        logger.debug("RoutingAttribute {} updated in RoutingAttributes pool", id);

        RoutingAttribute savedInDb = repository.save(routingAttribute);
        logger.debug("RoutingAttribute {} updated in RoutingAttributes Config DB", id);

        logger.info("RoutingAttribute {} updated successfully", id);
        return savedInDb;
    }

    @Override
    @Transactional
    public RoutingAttributeDeleteConflictResponse delete(String id) {
        logger.info("Delete RoutingAttribute request initiated with id: {}", id);

        if (!repository.existsById(id)) {
            String errorMessage = "Could not find RoutingAttribute: " + id + " resource to delete";
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueRepository.findByRoutingAttributeId(id);
        List<CCUser> agents = this.agentsRepository.findByRoutingAttributeId(id);

        if (precisionQueueEntities.isEmpty() && agents.isEmpty()) {
            this.routingAttributesPool.deleteById(id);
            logger.debug("RoutingAttribute {} deleted from RoutingAttributes in-memory pool", id);

            repository.deleteById(id);
            logger.debug("RoutingAttribute {} deleted from RoutingAttributes Config DB", id);

            logger.info("RoutingAttribute {} deleted successfully", id);
            return null;
        }

        logger.info("Could not delete RoutingAttribute {}, there are Queues or Agents associated to it", id);
        return new RoutingAttributeDeleteConflictResponse(precisionQueueEntities, agents);
    }

    @Override
    public Set<KeycloakUser> retrieveAgentsWithAssociatedRoutingAttributes(List<RoutingAttribute> routingAttributes) {
        logger.info("Request to retrieve agents with associated RoutingAttribute initiated.");

        var allAgentsWithAssociatedRoutingAttributes = routingAttributes.stream()
                .map(attr -> {
                    if (!routingAttributesPool.existsById(attr.getId())) {
                        String errorMessage = "Could not find RoutingAttribute : " + attr.getName() + " resource.";
                        logger.error(errorMessage);
                        throw new NotFoundException(errorMessage);
                    }

                    List<CCUser> usersEntityList = this.agentsRepository.findByRoutingAttributeId(attr.getId());
                    return usersEntityList;
                })
                .flatMap(ccUsersStream -> ccUsersStream.stream())
                .map(key -> key.getKeycloakUser())
                .collect(Collectors.toSet());

        logger.info("({}) agents retrieved successfully.", allAgentsWithAssociatedRoutingAttributes.size());
        return allAgentsWithAssociatedRoutingAttributes;
    }

    /**
     * Update precision queues.
     *
     * @param routingAttribute the routing attribute
     * @param id               the id
     */
    void updatePrecisionQueues(RoutingAttribute routingAttribute, String id) {
        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueRepository
                .findByRoutingAttributeId(id);
        if (precisionQueueEntities != null && !precisionQueueEntities.isEmpty()) {
            for (PrecisionQueueEntity precisionQueueEntity : precisionQueueEntities) {
                List<StepEntity> stepEntities = precisionQueueEntity.getSteps();
                if (stepEntities == null) {
                    continue;
                }
                for (StepEntity stepEntity : stepEntities) {
                    List<ExpressionEntity> expressionEntities = stepEntity.getExpressions();
                    if (expressionEntities == null) {
                        continue;
                    }
                    for (ExpressionEntity expressionEntity : expressionEntities) {
                        List<TermEntity> termEntities = expressionEntity.getTerms();
                        if (termEntities == null) {
                            continue;
                        }
                        for (TermEntity termEntity : termEntities) {
                            RoutingAttribute existingRoutingAttribute = termEntity.getRoutingAttribute();
                            if (existingRoutingAttribute == null) {
                                continue;
                            }
                            if (existingRoutingAttribute.getId().equals(id)) {
                                termEntity.setRoutingAttribute(routingAttribute);
                            }
                        }
                    }
                }
            }
            this.precisionQueueRepository.saveAll(precisionQueueEntities);
        }
    }

    /**
     * Update agents.
     *
     * @param routingAttribute the routing attribute
     * @param id               the id
     */
    boolean updateAgents(RoutingAttribute routingAttribute, String id) {
        List<CCUser> agents = this.agentsRepository.findByRoutingAttributeId(id);
        if (agents == null || agents.isEmpty()) {
            return false;
        }

        for (CCUser agent : agents) {
            List<AssociatedRoutingAttribute> associatedRoutingAttributes = agent
                    .getAssociatedRoutingAttributes();
            if (associatedRoutingAttributes == null) {
                continue;
            }
            for (AssociatedRoutingAttribute associatedRoutingAttribute : associatedRoutingAttributes) {
                RoutingAttribute existingRoutingAttribute = associatedRoutingAttribute
                        .getRoutingAttribute();
                if (existingRoutingAttribute == null) {
                    continue;
                }
                if (existingRoutingAttribute.getId().equals(id)) {
                    associatedRoutingAttribute.setRoutingAttribute(routingAttribute);
                }
            }
        }

        this.agentsRepository.saveAll(agents);
        return true;
    }
}
