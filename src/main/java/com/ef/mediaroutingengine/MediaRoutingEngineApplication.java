package com.ef.mediaroutingengine;

import com.ef.cim.objectmodel.*;
import com.ef.mediaroutingengine.Constants.GeneralConstants;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.redis.RedisClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.sql.Timestamp;
import java.util.UUID;

@SpringBootApplication
public class MediaRoutingEngineApplication {
	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(MediaRoutingEngineApplication.class, args);

		GeneralConstants.agentPresence = MediaRoutingEngineApplication.getAgentPresenceInstance();
		GeneralConstants.agentPresenceKey = "agentPresence:"+ GeneralConstants.agentPresence.getAgent().getId().toString();

		RedisClient redisClient = applicationContext.getBean(RedisClient.class);

		try {
			redisClient.setJSON(GeneralConstants.agentPresenceKey, GeneralConstants.agentPresence);
		} catch (Exception e){
			System.out.println("In main, while trying to put json in redis");
			e.printStackTrace();
		}
	}

	public static AgentPresence getAgentPresenceInstance(){
		Resource resource1 = new Resource();
		resource1.setRsid(UUID.randomUUID());
		resource1.setRsname("resource1");

		KeycloakUser keycloakUser = new KeycloakUser();
		keycloakUser.setId(UUID.randomUUID());
		keycloakUser.setFirstName("Ahmad");
		keycloakUser.setLastName("Bappi");
		keycloakUser.setRealm("realm1");
		keycloakUser.addRole("admin");
		keycloakUser.addPermittedResource(resource1);

		RoutingAttribute routingAttribute = new RoutingAttribute();
		routingAttribute.setId(UUID.randomUUID());
		routingAttribute.setName("attribute1");
		routingAttribute.setDescription("description");
		routingAttribute.setType(AttributeType.BOOLEAN);

		CCUser ccUser = new CCUser();
		ccUser.setKeycloakUser(keycloakUser);
		ccUser.addRoutingAttribute(routingAttribute);

		AgentPresence agentPresence = new AgentPresence();
		agentPresence.setAgent(ccUser);
		agentPresence.setState("created");
		agentPresence.addTopic("topic1");
		agentPresence.setStateChangeTime(new Timestamp(9000));

		return agentPresence;
	}

}
