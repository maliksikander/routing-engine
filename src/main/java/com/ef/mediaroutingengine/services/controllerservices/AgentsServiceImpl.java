package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.repositories.RoutingAttributeRepository;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentsServiceImpl implements AgentsService {

    private final AgentsRepository repository;
    private final RoutingAttributeRepository routingAttributeRepository;
    private final PrecisionQueuesPool precisionQueuesPool;

    /**
     * Constructor. Autowired, loads the beans.
     *
     * @param repository                 to communicate with Agents collection in the DB.
     * @param routingAttributeRepository to communicate with Routing-attributes collection in DB.
     * @param precisionQueuesPool        to communicate with Precision-Queue collection in redis-cache.
     */
    @Autowired
    public AgentsServiceImpl(AgentsRepository repository,
                             RoutingAttributeRepository routingAttributeRepository,
                             PrecisionQueuesPool precisionQueuesPool) {
        this.repository = repository;
        this.routingAttributeRepository = routingAttributeRepository;
        this.precisionQueuesPool = precisionQueuesPool;
    }

    @Override
    public CCUser create(CCUser agent) throws Exception {
        this.validateAndSetRoutingAttributes(agent);
        agent.setId(agent.getKeycloakUser().getId());
        CCUser saved = this.repository.insert(agent);
        this.precisionQueuesPool.evaluateAssociatedAgentsForAll();
        return saved;
    }

    @Override
    public List<CCUser> retrieve() {
        return this.repository.findAll();
    }

    @Override
    public CCUser update(CCUser agent, UUID id) throws Exception {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find agent resource to update");
        }

        this.validateAndSetRoutingAttributes(agent);
        agent.setId(id);
        CCUser saved = this.repository.save(agent);
        this.precisionQueuesPool.evaluateAssociatedAgentsForAll();
        return saved;
    }

    @Override
    public void delete(UUID id) throws Exception {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find agent resource to delete");
        }
        this.repository.deleteById(id);
        this.precisionQueuesPool.removeAgentFromAll(id);
    }

    private Map<UUID, RoutingAttribute> getRoutingAttributesFromDB() {
        List<RoutingAttribute> routingAttributes = routingAttributeRepository.findAll();
        Map<UUID, RoutingAttribute> routingAttributeMap = new HashMap<>();
        for (RoutingAttribute routingAttribute : routingAttributes) {
            routingAttributeMap.put(routingAttribute.getId(), routingAttribute);
        }
        return routingAttributeMap;
    }

    private void validateAndSetRoutingAttributes(CCUser agent) {
        List<AssociatedRoutingAttribute> associatedRoutingAttributes = agent.getAssociatedRoutingAttributes();
        if (associatedRoutingAttributes == null || associatedRoutingAttributes.isEmpty()) {
            return;
        }

        Map<UUID, RoutingAttribute> routingAttributes = this.getRoutingAttributesFromDB();

        for (AssociatedRoutingAttribute associatedRoutingAttribute : associatedRoutingAttributes) {
            RoutingAttribute routingAttribute = associatedRoutingAttribute.getRoutingAttribute();
            if (routingAttribute == null) {
                continue;
            }
            UUID routingAttributeId = routingAttribute.getId();
            if (routingAttributeId == null || !routingAttributes.containsKey(routingAttributeId)) {
                throw new NotFoundException("Could not find routing-attribute resource");
            }
            associatedRoutingAttribute.setRoutingAttribute(routingAttributes.get(routingAttribute.getId()));
        }
    }
}
