package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.repositories.RoutingAttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AgentsServiceImpl implements AgentsService{
    private final AgentsRepository repository;
    private final RoutingAttributeRepository routingAttributeRepository;

    @Autowired
    public AgentsServiceImpl(AgentsRepository repository, RoutingAttributeRepository routingAttributeRepository){
        this.repository = repository;
        this.routingAttributeRepository = routingAttributeRepository;
    }

    @Override
    public CCUser create(CCUser agent) {
        this.validateAndSetRoutingAttributes(agent);
        agent.setId(agent.getKeycloakUser().getId());
        return this.repository.insert(agent);
    }

    @Override
    public List<CCUser> retrieve() {
        return this.repository.findAll();
    }

    @Override
    public void update(CCUser agent, UUID id) {
        Optional<CCUser> existingObject = this.repository.findById(id);
        if(!existingObject.isPresent()){
            throw new NotFoundException("Could not find agent resource to update");
        }
        if(!agent.getKeycloakUser().equals(existingObject.get().getKeycloakUser())){
            throw new IllegalArgumentException("Can not update KeyCloakUser object");
        }

        this.validateAndSetRoutingAttributes(agent);
        agent.setId(id);
        this.repository.save(agent);
    }

    @Override
    public void delete(UUID id) {
        if(!this.repository.existsById(id)){
            throw new NotFoundException("Could not find agent resource to delete");
        }
        this.repository.deleteById(id);
    }

    private Map<UUID, RoutingAttribute> retrieveRoutingAttributes(){
        List<RoutingAttribute> routingAttributes = routingAttributeRepository.findAll();
        Map<UUID, RoutingAttribute> routingAttributeMap = new HashMap<>();
        for(RoutingAttribute routingAttribute: routingAttributes){
            routingAttributeMap.put(routingAttribute.getId(), routingAttribute);
        }
        return routingAttributeMap;
    }

    private void validateAndSetRoutingAttributes(CCUser agent){
        List<AssociatedRoutingAttribute> associatedRoutingAttributes = agent.getAssociatedRoutingAttributes();
        if(associatedRoutingAttributes==null || associatedRoutingAttributes.isEmpty()) return;

        Map<UUID, RoutingAttribute> routingAttributes = this.retrieveRoutingAttributes();

        for(AssociatedRoutingAttribute associatedRoutingAttribute: associatedRoutingAttributes){
            RoutingAttribute routingAttribute = associatedRoutingAttribute.getRoutingAttribute();
            if(routingAttribute == null) continue;
            UUID routingAttributeId = routingAttribute.getId();
            if(routingAttributeId==null || !routingAttributes.containsKey(routingAttributeId)){
                throw new NotFoundException("Could not find routing-attribute resource");
            }
            associatedRoutingAttribute.setRoutingAttribute(routingAttributes.get(routingAttribute.getId()));
        }
    }
}
