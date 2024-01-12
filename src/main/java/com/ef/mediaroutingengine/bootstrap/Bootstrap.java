package com.ef.mediaroutingengine.bootstrap;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.AssociatedMrd;
import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.ExpressionEntity;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.MrdType;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.StepEntity;
import com.ef.cim.objectmodel.TermEntity;
import com.ef.cim.objectmodel.enums.MrdTypeName;
import com.ef.cim.objectmodel.task.Task;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.pool.RoutingAttributesPool;
import com.ef.mediaroutingengine.routing.repository.AgentsRepository;
import com.ef.mediaroutingengine.routing.repository.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.routing.repository.MrdTypeRepository;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.routing.repository.RoutingAttributeRepository;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Bootstrap service is triggered when the Routing-Engine starts. It loads the routing-engine's
 * in-memory pools and Object states from the Configuration DB and Redis shared DB.
 * It also subscribes to the JMS topic to communicate state changes with Agent-Manager.
 */
@Service
public class Bootstrap {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    /**
     * Agents collection DAO for the Configuration DB.
     */
    private final AgentsRepository agentsRepository;
    /**
     * Media-Routing_Domains collection DAO for the Configuration DB.
     */
    private final MediaRoutingDomainRepository mediaRoutingDomainRepository;
    /**
     * Precision-Queues collection DAO for the Configuration DB.
     */
    private final PrecisionQueueRepository precisionQueueRepository;
    /**
     * Tasks collection DAO for Redis DB.
     */
    private final TasksRepository tasksRepository;
    /**
     * Agent-Presence collection DAO for the Redis DB.
     */
    private final AgentPresenceRepository agentPresenceRepository;
    /**
     * The Routing attribute repository.
     */
    private final RoutingAttributeRepository routingAttributeRepository;
    private final MrdTypeRepository mrdTypeRepository;
    /**
     * In-memory pool of all agents.
     */
    private final AgentsPool agentsPool;
    /**
     * In-memory pool of all MRDs.
     */
    private final MrdPool mrdPool;
    /**
     * In-memory pool of all Precision-Queues.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Routing attributes pool.
     */
    private final RoutingAttributesPool routingAttributesPool;
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;
    /**
     * Used here to Subscribe to the JMS Topic to communicate state changes with Agent-Manager.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Default Constructor. Loads the dependency beans.
     *
     * @param agentsRepository             Agents config repository DAO
     * @param mediaRoutingDomainRepository Media-routing-domains config repository DAO
     * @param precisionQueueRepository     Precision-Queues config repository DAO
     * @param tasksRepository              Tasks Repository DAO
     * @param agentPresenceRepository      AgentPresence Repository DAO
     * @param routingAttributeRepository   the routing attribute repository
     * @param agentsPool                   Agents Pool bean
     * @param mrdPool                      MRD Pool bean
     * @param precisionQueuesPool          Precision-Queues Pool bean
     * @param routingAttributesPool        the routing attributes pool
     * @param taskManager                  the task manager
     * @param jmsCommunicator              JMS Communicator
     */
    @Autowired
    public Bootstrap(AgentsRepository agentsRepository,
                     MediaRoutingDomainRepository mediaRoutingDomainRepository,
                     PrecisionQueueRepository precisionQueueRepository,
                     TasksRepository tasksRepository,
                     AgentPresenceRepository agentPresenceRepository,
                     RoutingAttributeRepository routingAttributeRepository,
                     MrdTypeRepository mrdTypeRepository,
                     AgentsPool agentsPool,
                     MrdPool mrdPool,
                     PrecisionQueuesPool precisionQueuesPool,
                     RoutingAttributesPool routingAttributesPool,
                     TaskManager taskManager,
                     JmsCommunicator jmsCommunicator) {
        this.agentsRepository = agentsRepository;
        this.mediaRoutingDomainRepository = mediaRoutingDomainRepository;
        this.precisionQueueRepository = precisionQueueRepository;
        this.tasksRepository = tasksRepository;
        this.agentPresenceRepository = agentPresenceRepository;
        this.routingAttributeRepository = routingAttributeRepository;
        this.mrdTypeRepository = mrdTypeRepository;
        this.agentsPool = agentsPool;
        this.mrdPool = mrdPool;
        this.precisionQueuesPool = precisionQueuesPool;
        this.routingAttributesPool = routingAttributesPool;
        this.taskManager = taskManager;
        this.jmsCommunicator = jmsCommunicator;
    }

    /**
     * Subscribes to state change Events JMS Topic to communicate state change with Agent-Manager.
     */
    public void subscribeToStateEventsChannel() {
        try {
            this.jmsCommunicator.init("STATE_CHANNEL", "conversation-topic");
            logger.info("Successfully subscribed to JMS topic: STATE_CHANNEL");
        } catch (JMSException jmsException) {
            logger.error(ExceptionUtils.getMessage(jmsException));
            logger.error(ExceptionUtils.getStackTrace(jmsException));
        }
    }

    /**
     * Loads the 'Agent', 'MRD', and 'Precision-Queue' in-memory pools from the configuration DB. Loads the
     * Agent and Agent-MRD states of Agents present in the REDIS AgentPresence DB. Initializes
     * the in-memory Task pool and loads the Tasks present in the REDIS Tasks DB at the time of startup.
     */
    public void loadPools() {
        logger.debug(Constants.METHOD_STARTED);
        // Load in-memory Routing-Attributes pool from Routing-Attributes Config DB.
        this.routingAttributesPool.loadFrom(routingAttributeRepository.findAll());
        logger.debug("Routing-Attributes pool loaded from DB");

        this.mrdPool.loadFrom(this.bootstrapMrdTypes(), this.getMrdFromConfigDb());
        logger.debug("MRDs pool loaded from DB");

        List<CCUser> ccUsers = this.getCcUsersFromConfigDb();
        this.agentsPool.loadFrom(ccUsers);
        // Set Agent and AgentMRD states after MRD pool is loaded as it is required for agent-mrd states.
        this.setAgentStates(ccUsers);
        logger.debug("Agents pool loaded DB");

        this.precisionQueuesPool.loadFrom(this.getQueuesFromConfigDb(), this.agentsPool);
        logger.debug("Precision-Queues pool loaded from DB");

        // Load tasks from Tasks Repository
        List<Task> tasks = this.tasksRepository.findAll();
        this.taskManager.loadTasksOnStartup(tasks);

        logger.info("Routing Attributes: {}", this.routingAttributesPool.size());
        logger.info("Agents: {}", this.agentsPool.size());
        logger.info("Media Routing Domains: {}", this.mrdPool.size());
        logger.info("Precision Queues: {}", this.precisionQueuesPool.size());
        logger.info("Tasks: {}", tasks.size());

        logger.debug(Constants.METHOD_ENDED);
    }

    private List<PrecisionQueueEntity> getQueuesFromConfigDb() {
        List<PrecisionQueueEntity> precisionQueueEntities = precisionQueueRepository.findAll();

        // Replace the routing-Attribute and MRD in Precision-Queues from that in the in-memory pool
        for (PrecisionQueueEntity entity : precisionQueueEntities) {
            // Replace MRD from in-memory pool
            MediaRoutingDomain mediaRoutingDomain = this.mrdPool.findById(entity.getMrd().getId());
            entity.setMrd(mediaRoutingDomain);
            // Replace Routing attribute from in-memory pool
            for (StepEntity step : entity.getSteps()) {
                for (ExpressionEntity expressionEntity : step.getExpressions()) {
                    for (TermEntity termEntity : expressionEntity.getTerms()) {
                        RoutingAttribute routingAttribute = this.routingAttributesPool
                                .findById(termEntity.getRoutingAttribute().getId());
                        termEntity.setRoutingAttribute(routingAttribute);
                    }
                }
            }
        }

        return precisionQueueEntities;
    }

    protected List<MediaRoutingDomain> getMrdFromConfigDb() {
        List<MediaRoutingDomain> mrdList = mediaRoutingDomainRepository.findAll();

        createAndSaveMrdIfNotExist(mrdList, createChatMrd());
        createAndSaveMrdIfNotExist(mrdList, createCiscoCcMrd());
        createAndSaveMrdIfNotExist(mrdList, createCxVoiceMrd());

        return mrdList;
    }

    private void createAndSaveMrdIfNotExist(List<MediaRoutingDomain> mediaRoutingDomainList, MediaRoutingDomain mrd) {
        if (!ifExist(mediaRoutingDomainList, mrd)) {
            this.mediaRoutingDomainRepository.save(mrd);
            mediaRoutingDomainList.add(mrd);
        }
    }

    private boolean ifExist(List<MediaRoutingDomain> mediaRoutingDomainList, MediaRoutingDomain mrd) {
        return mediaRoutingDomainList.stream().anyMatch(existingMrd -> existingMrd.getId().equals(mrd.getId()));
    }

    protected MediaRoutingDomain createCiscoCcMrd() {
        return new MediaRoutingDomain(Constants.CISCO_CC_MRD_ID, Constants.CISCO_CC_MRD_TYPE_ID,
                "CISCO CC", "Standard voice MRD for CISCO CC", 1);
    }

    protected MediaRoutingDomain createCxVoiceMrd() {
        return new MediaRoutingDomain(Constants.CX_VOICE_MRD_ID, Constants.CX_VOICE_MRD_TYPE_ID,
                "CX VOICE", "Standard voice MRD for CX Voice", 1);
    }

    protected MediaRoutingDomain createChatMrd() {
        return new MediaRoutingDomain(Constants.CHAT_MRD_ID, Constants.CHAT_MRD_TYPE_ID,
                "CHAT", "Standard chat MRD", 5);
    }

    List<MrdType> bootstrapMrdTypes() {
        List<MrdType> mrdTypeList = this.mrdTypeRepository.findAll();
        List<String> idList = mrdTypeList.stream().map(MrdType::getId).toList();

        if (!idList.contains(Constants.CHAT_MRD_TYPE_ID)) {
            mrdTypeList.add(new MrdType(Constants.CHAT_MRD_TYPE_ID, MrdTypeName.CHAT, true, true, true));
        }
        if (!idList.contains(Constants.CX_VOICE_MRD_TYPE_ID)) {
            mrdTypeList.add(new MrdType(Constants.CX_VOICE_MRD_TYPE_ID, MrdTypeName.CX_VOICE, true, false, false));
        }
        if (!idList.contains(Constants.CISCO_CC_MRD_TYPE_ID)) {
            mrdTypeList.add(new MrdType(Constants.CISCO_CC_MRD_TYPE_ID, MrdTypeName.CISCO_CC, false, false, false));
        }

        return this.mrdTypeRepository.saveAll(mrdTypeList);
    }

    private List<CCUser> getCcUsersFromConfigDb() {
        List<CCUser> ccUsers = agentsRepository.findAll();

        // Replace Routing attributes in CcUsers with in-memory objects for object sharing
        for (CCUser ccUser : ccUsers) {
            for (AssociatedRoutingAttribute entry : ccUser.getAssociatedRoutingAttributes()) {
                RoutingAttribute routingAttribute = this.routingAttributesPool
                        .findById(entry.getRoutingAttribute().getId());
                entry.setRoutingAttribute(routingAttribute);
            }
        }

        return ccUsers;
    }

    /**
     * Returns the List of AgentMrdStates with initial state set (NOT_READY)
     * for each MRD in the parameter list.
     *
     * @param mediaRoutingDomains list of MRDs for which the AgentMrdState is required.
     * @return List of AgentMrdStates which state set to an initial value (NOT_READY)
     */
    private List<AgentMrdState> getInitialAgentMrdStates(CCUser ccUser, List<MediaRoutingDomain> mediaRoutingDomains) {
        List<AgentMrdState> agentMrdStates = new ArrayList<>();
        for (MediaRoutingDomain mrd : mediaRoutingDomains) {
            int maxAgentRequests = this.getMaxAgentRequests(mrd, ccUser);

            AgentMrdState agentMrdState = new AgentMrdState(mrd, Enums.AgentMrdStateName.NOT_READY);
            agentMrdState.setMaxAgentTasks(maxAgentRequests);

            agentMrdStates.add(agentMrdState);
        }
        return agentMrdStates;
    }

    private int getMaxAgentRequests(MediaRoutingDomain mrd, CCUser ccUser) {
        for (AssociatedMrd associatedMrd : ccUser.getAssociatedMrds()) {
            if (associatedMrd.getMrdId().equals(mrd.getId())) {
                return associatedMrd.getMaxAgentTasks();
            }
        }

        return mrd.getMaxRequests();
    }

    /**
     * Gets AgentPresence list from Redis AgentPresence Collection and converts it to a Map of
     * agentId:AgentPresence.
     *
     * @return Map of AgentPresence Objects with agentId as the key.
     */
    private Map<String, AgentPresence> getCurrentAgentPresenceMap() {
        List<AgentPresence> currentAgentPresenceList = this.agentPresenceRepository.findAll(2500);
        logger.debug("Fetched List of all AgentPresence objects from Redis Collection successfully");
        Map<String, AgentPresence> currentAgentPresenceMap = new HashMap<>();
        for (AgentPresence agentPresence : currentAgentPresenceList) {
            currentAgentPresenceMap.put(agentPresence.getAgent().getId(), agentPresence);
        }
        return currentAgentPresenceMap;
    }

    /**
     * Checks if the AgentMRDState found from an AgentPresence Object in Redis exists in the
     * AgentMrdStateList. If so it updates the Agent-MRD-state object present in the list with
     * the one fetched from the Agent-Presence.
     *
     * @param agentMrdStateInAgentPresence Agent-MRD-state found in AgentPresence collection for an Agent
     * @param agentMrdStates               List of AgentMrdStates that needs to be updated if Mrd-State found.
     */
    private void updateMrdStateFromAgentPresence(AgentMrdState agentMrdStateInAgentPresence,
                                                 List<AgentMrdState> agentMrdStates) {
        for (int i = 0; i < agentMrdStates.size(); i++) {
            AgentMrdState agentMrdStateInList = agentMrdStates.get(i);
            if (agentMrdStateInAgentPresence.getMrd().getId().equals(agentMrdStateInList.getMrd().getId())) {
                agentMrdStateInAgentPresence.setMrd(agentMrdStateInList.getMrd());
                agentMrdStateInAgentPresence.setMaxAgentTasks(agentMrdStateInList.getMaxAgentTasks());
                agentMrdStates.set(i, agentMrdStateInAgentPresence);
                break;
            }
        }
    }

    /**
     * Sets the Agent and Agent-MRD states of the agents in the in-memory Agents-pool. If a new agent is found
     * that has not been present in the AgentPresence Redis Collection previously, the 'initial' Agent /
     * Agent-MRD states will be set for this agent. If the agent is already present in the AgentPresence
     * collection, the 'previous' states will be fetched from the Redis collection and set for this agent.
     */
    private void setAgentStates(List<CCUser> ccUsers) {
        Map<String, AgentPresence> currentAgentPresenceMap = this.getCurrentAgentPresenceMap();
        // AgentPresence Repository is flushed so that newly-updated, fresh Objects are added.
        this.agentPresenceRepository.deleteAll();
        logger.debug("AgentPresence Repository flushed successfully.");
        Map<String, AgentPresence> updatedAgentPresenceMap = new HashMap<>();

        for (CCUser ccUser : ccUsers) {
            Agent agent = agentsPool.findBy(ccUser.getKeycloakUser().getId());

            AgentState agentState;
            List<AgentMrdState> agentMrdStates = getInitialAgentMrdStates(ccUser, this.mrdPool.findAll());
            AgentPresence agentPresence = currentAgentPresenceMap.get(agent.getId());

            if (agentPresence != null) {
                agentState = agentPresence.getState();
                for (AgentMrdState agentMrdState : agentPresence.getAgentMrdStates()) {
                    this.updateMrdStateFromAgentPresence(agentMrdState, agentMrdStates);
                }
                agentPresence.setAgent(agent.toCcUser());
                agentPresence.setAgentMrdStates(agentMrdStates);
            } else {
                agentState = new AgentState(Enums.AgentStateName.LOGOUT, null);
                agentPresence = new AgentPresence(agent.toCcUser(), agentState, agentMrdStates);
            }
            agent.setState(agentState);
            agent.setAgentMrdStates(agentMrdStates);
            updatedAgentPresenceMap.put(agentPresence.getAgent().getId(), agentPresence);
        }

        logger.debug("Agent states for agents in in-memory pool set successfully");
        this.agentPresenceRepository.saveAllByKeyValueMap(updatedAgentPresenceMap, 2500);
        logger.debug("AgentPresence Repository loaded successfully");
    }
}

