package com.ef.mediaroutingengine;

import static org.junit.jupiter.api.Assertions.assertTrue;


import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.RoutingAttributeType;
import com.ef.mediaroutingengine.model.Agent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.junit.jupiter.api.Test;

class ScriptEngineTest {

    private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");

    @Test
    void testScriptEngine() throws ScriptException {
        UUID routingAttributeId = UUID.fromString("b590d3eb-f727-45a6-beb7-73671e60d8ac");
        RoutingAttribute routingAttribute = getRoutingAttribute("English",
                RoutingAttributeType.PROFICIENCY_LEVEL, 5);
        routingAttribute.setId(routingAttributeId);

        AssociatedRoutingAttribute associatedRoutingAttribute = new AssociatedRoutingAttribute();
        associatedRoutingAttribute.setRoutingAttribute(routingAttribute);
        associatedRoutingAttribute.setValue(7);

        RoutingAttribute routingAttribute2 = getRoutingAttribute("Sales", RoutingAttributeType.BOOLEAN,
                1);
        routingAttribute2.setId(UUID.fromString("2a1bbdc6-7494-4545-a54d-1c54c39eea6b"));

        AssociatedRoutingAttribute associatedRoutingAttribute2 = new AssociatedRoutingAttribute();
        associatedRoutingAttribute2.setRoutingAttribute(routingAttribute2);
        associatedRoutingAttribute2.setValue(1);

        List<AssociatedRoutingAttribute> associatedRoutingAttributes = new ArrayList<>();
        associatedRoutingAttributes.add(associatedRoutingAttribute);
        associatedRoutingAttributes.add(associatedRoutingAttribute2);

        KeycloakUser keycloakUser = getKeycloakUser("Ahmad", "Ali");
        CCUser ccUser = new CCUser();
        ccUser.setId(keycloakUser.getId());
        ccUser.setKeycloakUser(keycloakUser);
        ccUser.setAssociatedRoutingAttributes(associatedRoutingAttributes);

        Agent agent = new Agent(ccUser);
        String criteria = "agent.associatedRoutingAttributes['" + routingAttributeId + "'].value>5";

        scriptEngine.put("agent", agent);
        assertTrue((Boolean) scriptEngine.eval(criteria));
    }

    private KeycloakUser getKeycloakUser(String firstname, String lastname) {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
        keycloakUser.setRoles(new ArrayList<>());
        keycloakUser.setPermittedResources(null);
        keycloakUser.setRealm("realm1");
        keycloakUser.setFirstName(firstname);
        keycloakUser.setLastName(lastname);
        keycloakUser.setUsername(firstname + "-" + lastname);
        return keycloakUser;
    }

    private RoutingAttribute getRoutingAttribute(String name, RoutingAttributeType type, int defaultValue) {
        RoutingAttribute routingAttribute = new RoutingAttribute();
        routingAttribute.setId(UUID.randomUUID());
        routingAttribute.setName(name);
        routingAttribute.setDescription("Description-" + name);
        routingAttribute.setType(type);
        routingAttribute.setDefaultValue(defaultValue);
        return routingAttribute;
    }
}
