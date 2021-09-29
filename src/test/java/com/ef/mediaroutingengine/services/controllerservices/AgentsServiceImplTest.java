package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.RoutingAttributeType;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.RoutingAttributesPool;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentsServiceImplTest {
    AgentsServiceImpl agentsService;
    @Mock
    private AgentsRepository repository;
    @Mock
    private RoutingAttributesPool routingAttributesPool;
    @Mock
    private AgentsPool agentsPool;
    @Mock
    private MrdPool mrdPool;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private AgentPresenceRepository agentPresenceRepository;

    @BeforeEach
    void setUp() {
        this.agentsService = new AgentsServiceImpl(repository, routingAttributesPool, agentsPool,
                mrdPool, precisionQueuesPool, agentPresenceRepository);
    }

    @Test
    void testValidateAndSetRoutingAttributes() {
        CCUser ccUser = getNewCcUser();


    }

    private CCUser getNewCcUser() {
        List<AssociatedRoutingAttribute> attributes = new ArrayList<>();
        attributes.add(getNewAssociatedAttribute("Sales", RoutingAttributeType.BOOLEAN, 1));
        attributes.add(getNewAssociatedAttribute("English", RoutingAttributeType.PROFICIENCY_LEVEL, 7));
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(getNewKeyClockUser());
        ccUser.setAssociatedRoutingAttributes(attributes);
        return ccUser;
    }

    private KeycloakUser getNewKeyClockUser() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
        return keycloakUser;
    }

    private RoutingAttribute getNewAttribute(String name, RoutingAttributeType type) {
        RoutingAttribute routingAttribute = new RoutingAttribute();
        routingAttribute.setId(UUID.randomUUID().toString());
        routingAttribute.setName(name);
        routingAttribute.setDescription(name + "desc");
        routingAttribute.setType(type);
        routingAttribute.setDefaultValue(1);
        return routingAttribute;
    }

    private AssociatedRoutingAttribute getNewAssociatedAttribute(String name, RoutingAttributeType type, int value) {
        AssociatedRoutingAttribute associatedRoutingAttribute = new AssociatedRoutingAttribute();
        associatedRoutingAttribute.setRoutingAttribute(getNewAttribute(name, type));
        associatedRoutingAttribute.setValue(value);
        return associatedRoutingAttribute;
    }
}