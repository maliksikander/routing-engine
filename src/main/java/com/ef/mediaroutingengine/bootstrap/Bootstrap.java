package com.ef.mediaroutingengine.bootstrap;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.mediaroutingengine.commons.Constants;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.model.ExpressionEntity;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.StepEntity;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TermEntity;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.repositories.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueRepository;
import com.ef.mediaroutingengine.repositories.RoutingAttributeRepository;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.RoutingAttributesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Bootstrap service is triggerred when the Routing-Engine starts. It loads the routing-engine's
 * in-memory pools and Object states from the Configuration DB and Redis shared DB.
 * It also subscribes to the JMS topic to communicate state changes with Agent-Manager.
 */
@Service
public class Bootstrap {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);
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
     * In-memory pool of all Tasks.
     */
    private final TasksPool tasksPool;
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
     * @param agentsRepository               Agents config repository DAO
     * @param mediaRoutingDomainRepository   Media-routing-domains config repository DAO
     * @param precisionQueueRepository Precision-Queues config repository DAO
     * @param tasksRepository                Tasks Repository DAO
     * @param agentPresenceRepository        AgentPresence Repository DAO
     * @param routingAttributeRepository     the routing attribute repository
     * @param agentsPool                     Agents Pool bean
     * @param mrdPool                        MRD Pool bean
     * @param precisionQueuesPool            Precision-Queues Pool bean
     * @param routingAttributesPool          the routing attributes pool
     * @param tasksPool                      Tasks pool bean
     * @param taskManager                    the task manager
     * @param jmsCommunicator                JMS Communicator
     */
    @Autowired
    public Bootstrap(AgentsRepository agentsRepository,
                     MediaRoutingDomainRepository mediaRoutingDomainRepository,
                     PrecisionQueueRepository precisionQueueRepository,
                     TasksRepository tasksRepository,
                     AgentPresenceRepository agentPresenceRepository,
                     RoutingAttributeRepository routingAttributeRepository,
                     AgentsPool agentsPool,
                     MrdPool mrdPool,
                     PrecisionQueuesPool precisionQueuesPool,
                     RoutingAttributesPool routingAttributesPool,
                     TasksPool tasksPool,
                     TaskManager taskManager,
                     JmsCommunicator jmsCommunicator) {
        this.agentsRepository = agentsRepository;
        this.mediaRoutingDomainRepository = mediaRoutingDomainRepository;
        this.precisionQueueRepository = precisionQueueRepository;
        this.tasksRepository = tasksRepository;
        this.agentPresenceRepository = agentPresenceRepository;
        this.routingAttributeRepository = routingAttributeRepository;
        this.agentsPool = agentsPool;
        this.mrdPool = mrdPool;
        this.precisionQueuesPool = precisionQueuesPool;
        this.routingAttributesPool = routingAttributesPool;
        this.tasksPool = tasksPool;
        this.taskManager = taskManager;
        this.jmsCommunicator = jmsCommunicator;
    }

    /**
     * Subscribes to state change Events JMS Topic to communicate state change with Agent-Manager.
     *
     * @return the boolean
     */
    public boolean subscribeToStateEventsChannel() {
        try {
            this.jmsCommunicator.init("STATE_CHANNEL");
            LOGGER.info("JMS topic subscribed successfully");
            return true;
        } catch (JMSException jmsException) {
            LOGGER.error("JmsException while initializing JMS-Communicator: ", jmsException);
        }
        LOGGER.debug("Failed to subscribe JMS topic ");
        return false;
    }

    /**
     * Loads the 'Agent', 'MRD', and 'Precision-Queue' in-memory pools from the configuration DB. Loads the
     * Agent and Agent-MRD states of Agents present in the REDIS AgentPresence DB. Initializes
     * the in-memory Task pool and loads the Tasks present in the REDIS Tasks DB at the time of startup.
     */
    public void loadPools() {
        LOGGER.debug(Constants.METHOD_STARTED);
        // Load in-memory Routing-Attributes pool from Routing-Attributes Config DB.
        this.routingAttributesPool.loadFrom(routingAttributeRepository.findAll());

        List<CCUser> ccUsers = agentsRepository.findAll();
        /*
        Replace the routing-Attribute in CC-Users from that in the in-memory routing-attribute pool
        Advantage: Shared memory: we update routing-Attribute in pool, it is updated every-where it is
                    being used. (CCUsers - in this case)
         */
        this.replaceRoutingAttributesInCcUsers(ccUsers);

        this.agentsPool.loadPoolFrom(ccUsers);
        LOGGER.debug("Agents pool loaded");

        this.mrdPool.loadPoolFrom(mediaRoutingDomainRepository.findAll());
        LOGGER.debug("MRDs pool loaded");
        // Set Agent and AgentMRD states after MRD pool is loaded as it is required for agent-mrd states.
        this.setAgentStates();
        /*
        Load Precision-Queue pool. Requires Agents pool to be loaded first to evaluate the Agents
        associated to steps in the Precision-Queue.
         */
        List<PrecisionQueueEntity> precisionQueueEntities = precisionQueueRepository.findAll();
        /*
        Replace the routing-Attribute and MRD in Precision-Queues from that in the in-memory pool
        Advantage: Shared memory: we update routing-Attribute / MRD in pools, it is updated every-where it is
                    being used. (Precision-Queues -> Steps - in this case)
         */
        this.replaceRoutingAttributesAndMrdInQueues(precisionQueueEntities);
        this.precisionQueuesPool.loadPoolFrom(precisionQueueEntities, this.agentsPool);
        LOGGER.debug("Precision-Queues pool loaded");

        /*
        Load the in-memory Tasks pool from REDIS.
        Associate tasks to the (in-memory) Agents, if a task has been previously assigned to an agent.
        (Requires Agent pool to be loaded first ^)
        Requires Precision-Queue pool to be loaded first as it enqueues the Tasks with QUEUED state
         */
        List<TaskDto> taskDtoList = this.tasksRepository.findAll();
        for (TaskDto taskDto : taskDtoList) {
            this.replaceMrdInTask(taskDto);
            Task task = new Task(taskDto);
            this.associateTaskWithAgent(task);
            if (task.getRoutingMode().equals(RoutingMode.PUSH)) {
                this.taskManager.enqueueTask(task);
            } else if (task.getRoutingMode().equals(RoutingMode.PULL)) {
                this.tasksPool.add(task);
            }
        }

        LOGGER.info("Agents pool size: {}", this.agentsPool.size());
        LOGGER.info("Mrd pool size: {}", this.mrdPool.size());
        LOGGER.info("Precision-Queues pool size: {}", this.precisionQueuesPool.size());
        LOGGER.info("Task pool size: {}", this.tasksPool.size());

        LOGGER.debug(Constants.METHOD_ENDED);
    }

    /**
     * Replace mrd in task.
     *
     * @param taskDto the task dto
     */
    private void replaceMrdInTask(TaskDto taskDto) {
        MediaRoutingDomain mediaRoutingDomain = this.mrdPool.findById(taskDto.getMrd().getId());
        taskDto.setMrd(mediaRoutingDomain);
    }

    /**
     * Replace routing attributes and mrd in queues.
     *
     * @param precisionQueueEntities the precision queue entities
     */
    private void replaceRoutingAttributesAndMrdInQueues(List<PrecisionQueueEntity> precisionQueueEntities) {
        for (PrecisionQueueEntity entity : precisionQueueEntities) {
            MediaRoutingDomain mediaRoutingDomain = this.mrdPool.findById(entity.getMrd().getId());
            entity.setMrd(mediaRoutingDomain);
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
    }

    /**
     * Replace routing attributes in cc users.
     *
     * @param ccUsers the cc users
     */
    private void replaceRoutingAttributesInCcUsers(List<CCUser> ccUsers) {
        for (CCUser ccUser : ccUsers) {
            for (AssociatedRoutingAttribute entry : ccUser.getAssociatedRoutingAttributes()) {
                RoutingAttribute routingAttribute = this.routingAttributesPool
                        .findById(entry.getRoutingAttribute().getId());
                entry.setRoutingAttribute(routingAttribute);
            }
        }
    }

    /**
     * Adds a task to the Agent's active tasks list or reserved task object (depending on the task state).
     * This agent should be assigned to the task previously.
     *
     * @param task the task to be associated with the agent
     */
    private void associateTaskWithAgent(Task task) {
        LOGGER.debug(Constants.METHOD_STARTED);
        Agent agent = this.agentsPool.findById(task.getAssignedTo());
        if (agent != null) {
            LOGGER.debug("Agent: {} assigned to the Task found", agent.getId());
            if (task.getTaskState().getName().equals(Enums.TaskStateName.RESERVED)) {
                agent.reserveTask(task);
                LOGGER.debug("Task: {} reserved for Agent: {}", task.getId(), agent.getId());
            } else if (task.getTaskState().getName().equals(Enums.TaskStateName.ACTIVE)) {
                agent.addActiveTask(task);
                LOGGER.debug("Task: {} added to the Agent: {} active tasks list", task.getId(), agent.getId());
            }
        } else {
            LOGGER.debug("No agent assigned to Task: {}", task.getId());
        }
        LOGGER.debug(Constants.METHOD_ENDED);
    }

    /**
     * Returns the List of AgentMrdStates with initial state set (NOT_READY)
     * for each MRD in the parameter list.
     *
     * @param mediaRoutingDomains list of MRDs for which the AgentMrdState is required.
     * @return List of AgentMrdStates which state set to an initial value (NOT_READY)
     */
    private List<AgentMrdState> getInitialAgentMrdStates(List<MediaRoutingDomain> mediaRoutingDomains) {
        List<AgentMrdState> agentMrdStates = new ArrayList<>();
        for (MediaRoutingDomain mrd : mediaRoutingDomains) {
            agentMrdStates.add(new AgentMrdState(mrd, Enums.AgentMrdStateName.NOT_READY));
        }
        return agentMrdStates;
    }

    /**
     * Gets AgentPresence list from Redis AgentPresence Collection and converts it to a Map of
     * agentId:AgentPresence.
     *
     * @return Map of AgentPresence Objects with agentId as the key.
     */
    private Map<UUID, AgentPresence> getCurrentAgentPresenceMap() {
        List<AgentPresence> currentAgentPresenceList = this.agentPresenceRepository.findAll();
        LOGGER.debug("Fetched List of all AgentPresence objects from Redis Collection successfully");
        Map<UUID, AgentPresence> currentAgentPresenceMap = new HashMap<>();
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
    private void setAgentStates() {
        Map<UUID, AgentPresence> currentAgentPresenceMap = this.getCurrentAgentPresenceMap();
        // AgentPresence Repository is flushed so that newly-updated, fresh Objects are added.
        this.agentPresenceRepository.deleteAll();
        LOGGER.debug("AgentPresence Repository flushed successfully.");
        Map<String, AgentPresence> updatedAgentPresenceMap = new HashMap<>();
        for (Agent agent : this.agentsPool.findAll()) {
            AgentState agentState;
            List<AgentMrdState> agentMrdStates = getInitialAgentMrdStates(this.mrdPool.findAll());
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
            updatedAgentPresenceMap.put(agentPresence.getAgent().getId().toString(), agentPresence);
        }
        LOGGER.debug("Agent states for agents in in-memory pool set successfully");
        this.agentPresenceRepository.saveAllByKeyValueMap(updatedAgentPresenceMap);
        LOGGER.debug("AgentPresence Repository loaded successfully");
    }
}

