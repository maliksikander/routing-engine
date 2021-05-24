package com.ef.mediaroutingengine;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.RoutingAttributeType;
import com.ef.mediaroutingengine.constants.GeneralConstants;
import com.ef.mediaroutingengine.dto.RedisEvent;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.services.redis.ChannelSubscribe;
import com.ef.mediaroutingengine.services.redis.RedisClient;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

@SpringBootApplication
public class MediaRoutingEngineApplication {

    /**
     * application's starting point.
     *
     * @param args list of command line arguments
     */
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(MediaRoutingEngineApplication.class, args);
        ChannelSubscribe channelSubscribe = applicationContext.getBean(ChannelSubscribe.class);
        JedisPool jedisPool = applicationContext.getBean(JedisPool.class);

        JedisPubSub jedisPubSub = channelSubscribe.getJedisPubSubInstance();
        jedisPool.getResource().subscribe(jedisPubSub, "REDIS_MESSAGE_CHANNEL");
    }

    private static void addTask(ApplicationContext applicationContext) {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setDescription("Description 1");
        mrd.setId(UUID.randomUUID());
        mrd.setName("MRD1");
        mrd.setInterruptible(false);

        TaskDto task = new TaskDto();
        task.setId(UUID.randomUUID());
        task.setState(Enums.TaskState.CREATED);
        task.setPriority(1);
        task.setMrd(mrd);
    }

    private static void putOneAgentInAgentPresenceCache(ApplicationContext context) {
        GeneralConstants.setAgentPresence(MediaRoutingEngineApplication.getAgentPresenceInstance());
        GeneralConstants.setAgentPresenceKey(
                "agentPresence:" + GeneralConstants.getAgentPresence().getAgent().getId().toString());

        RedisClient redisClient = context.getBean(RedisClient.class);

        try {
            redisClient.setJson(GeneralConstants.getAgentPresenceKey(),
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
        routingAttribute.setType(RoutingAttributeType.BOOLEAN);
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
