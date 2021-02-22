package com.ef.mediaroutingengine;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.AttributeType;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.constants.GeneralConstants;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.services.UserAudtiting;
import com.ef.mediaroutingengine.services.redis.RedisClient;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing(auditorAwareRef = "happyg")
public class MediaRoutingEngineApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication
                .run(MediaRoutingEngineApplication.class, args);

        UserAudtiting userAudtiting = applicationContext.getBean(UserAudtiting.class);
        System.out.println(userAudtiting.getClass());

        GeneralConstants.agentPresence = MediaRoutingEngineApplication.getAgentPresenceInstance();
        GeneralConstants.agentPresenceKey =
                "agentPresence:" + GeneralConstants.agentPresence.getAgent().getId().toString();

        RedisClient redisClient = applicationContext.getBean(RedisClient.class);

        try {
            redisClient.setJSON(GeneralConstants.agentPresenceKey, GeneralConstants.agentPresence);
        } catch (Exception e) {
            System.out.println("In main, while trying to put json in redis");
            e.printStackTrace();
        }
    }

    public static AgentPresence getAgentPresenceInstance() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
        keycloakUser.setFirstName("Ahmad");
        keycloakUser.setLastName("Bappi");
        keycloakUser.addRole("admin");

        RoutingAttribute routingAttribute = new RoutingAttribute();
        routingAttribute.setId(UUID.randomUUID());
        routingAttribute.setName("attribute1");
        routingAttribute.setDescription("description");
        routingAttribute.setType(AttributeType.BOOLEAN);
        routingAttribute.setDefaultValue("true");

        AssociatedRoutingAttribute associatedRoutingAttribute = new AssociatedRoutingAttribute();
        associatedRoutingAttribute.setRoutingAttribute(routingAttribute);
        associatedRoutingAttribute.setValue(routingAttribute.getDefaultValue());

        CCUser ccUser = new CCUser();
        ccUser.setId(keycloakUser.getId());
        ccUser.setKeycloakUser(keycloakUser);
        ccUser.addAssociatedRoutingAttribute(associatedRoutingAttribute);

        AgentPresence agentPresence = new AgentPresence();
        agentPresence.setAgent(ccUser);
        agentPresence.setState("created");
        agentPresence.addTopic("topic1");
        agentPresence.setStateChangeTime(new Timestamp(9000));

        return agentPresence;
    }

}
