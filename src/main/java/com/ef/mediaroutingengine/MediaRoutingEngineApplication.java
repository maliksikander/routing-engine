package com.ef.mediaroutingengine;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.AttributeType;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.constants.GeneralConstants;
import com.ef.mediaroutingengine.dto.RedisEvent;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.CommonEnums;
import com.ef.mediaroutingengine.services.redis.RedisClient;
import com.ef.mediaroutingengine.services.redispubsub.MessagePublisher;
import com.ef.mediaroutingengine.services.redispubsub.RedisMessagePublisher;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class MediaRoutingEngineApplication {

    /**
     * application's starting point.
     *
     * @param args list of command line arguments
     */
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(MediaRoutingEngineApplication.class, args);
        MessagePublisher publisher = applicationContext.getBean(MessagePublisher.class);
        RedisEvent redisEvent = new RedisEvent();
        redisEvent.setName(CommonEnums.RedisEventName.TASK_STATE_CHANGED);
        redisEvent.setData("DATA");
        publisher.publish(redisEvent);
        //putOneAgentInAgentPresenceCache(applicationContext);
    }

    private static void putOneAgentInAgentPresenceCache(ApplicationContext context) {
        GeneralConstants.setAgentPresence(MediaRoutingEngineApplication.getAgentPresenceInstance());
        GeneralConstants.setAgentPresenceKey(
                "agentPresence:" + GeneralConstants.getAgentPresence().getAgent().getId().toString());

        RedisClient redisClient = context.getBean(RedisClient.class);

        try {
            redisClient.setJSON(GeneralConstants.getAgentPresenceKey(),
                    GeneralConstants.getAgentPresence());
        } catch (Exception e) {
            System.out.println("In main, while trying to put json in redis");
            e.printStackTrace();
        }
    }

    /**
     * Creates a AgentPresence instance.
     *
     * @return AgentPresence
     */
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
        routingAttribute.setDefaultValue(1);

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
