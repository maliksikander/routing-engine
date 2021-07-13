package com.ef.mediaroutingengine.bootstrap;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.repositories.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueEntityRepository;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
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

@Service
public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private final AgentsRepository agentsRepository;
    private final MediaRoutingDomainRepository mediaRoutingDomainRepository;
    private final PrecisionQueueEntityRepository precisionQueueEntityRepository;
    private final TasksRepository tasksRepository;
    private final AgentPresenceRepository agentPresenceRepository;

    private final AgentsPool agentsPool;
    private final MrdPool mrdPool;
    private final PrecisionQueuesPool precisionQueuesPool;
    private final TasksPool tasksPool;

    private final JmsCommunicator jmsCommunicator;

    /**
     * Constructor. Loads the required beans.
     *
     * @param agentsRepository               Agents config repository DAO
     * @param mediaRoutingDomainRepository   Media-routing-domains config repository DAO
     * @param precisionQueueEntityRepository Precision-Queues config repository DAO
     * @param tasksRepository                Tasks Repository DAO
     * @param agentsPool                     Agents Pool bean
     * @param mrdPool                        MRD Pool bean
     * @param precisionQueuesPool            Precision-Queues Pool bean
     * @param tasksPool                      Tasks pool bean
     * @param jmsCommunicator                JMS Communicator
     */
    @Autowired
    public Bootstrap(AgentsRepository agentsRepository,
                       MediaRoutingDomainRepository mediaRoutingDomainRepository,
                       PrecisionQueueEntityRepository precisionQueueEntityRepository,
                       TasksRepository tasksRepository,
                       AgentPresenceRepository agentPresenceRepository,
                       AgentsPool agentsPool,
                       MrdPool mrdPool,
                       PrecisionQueuesPool precisionQueuesPool,
                       TasksPool tasksPool,
                       JmsCommunicator jmsCommunicator) {
        this.agentsRepository = agentsRepository;
        this.mediaRoutingDomainRepository = mediaRoutingDomainRepository;
        this.precisionQueueEntityRepository = precisionQueueEntityRepository;
        this.tasksRepository = tasksRepository;
        this.agentPresenceRepository = agentPresenceRepository;
        this.agentsPool = agentsPool;
        this.mrdPool = mrdPool;
        this.precisionQueuesPool = precisionQueuesPool;
        this.tasksPool = tasksPool;
        this.jmsCommunicator = jmsCommunicator;
    }

    /**
     * Loads All Pools at start of the application.
     */
    public void loadPools() {
        //TODO: Load Agent / Agent MRD states from Redis.
        List<CCUser> ccUsers = agentsRepository.findAll();
        this.agentsPool.loadPoolFrom(ccUsers);

        List<MediaRoutingDomain> mediaRoutingDomains = mediaRoutingDomainRepository.findAll();
        this.mrdPool.loadPoolFrom(mediaRoutingDomains);

        this.loadAgentPresenceDb(mediaRoutingDomains, ccUsers);
        Map<UUID, AgentPresence> agentPresenceMap = this.getAgentPresenceMap();
        for (Agent agent : this.agentsPool.toList()) {
            AgentPresence agentPresence = agentPresenceMap.get(agent.getId());
            agent.setState(agentPresence.getState());
            agent.setAgentMrdStates(agentPresence.getAgentMrdStates());
        }

        List<PrecisionQueueEntity> precisionQueueEntities = precisionQueueEntityRepository.findAll();
        this.precisionQueuesPool.loadPoolFrom(precisionQueueEntities);

        List<TaskDto> taskDtoList = this.tasksRepository.findAll();
        for (TaskDto taskDto : taskDtoList) {
            this.tasksPool.enqueueTask(new Task(taskDto));
        }

        LOGGER.info("Agents pool size: {}", this.agentsPool.size());
        LOGGER.info("Mrd pool size: {}", this.mrdPool.size());
        LOGGER.info("Precision-Queues pool size: {}", this.precisionQueuesPool.size());
        LOGGER.info("Task pool size: {}", this.tasksPool.size());
    }

    /**
     * Subscribes to state change Events JMS Topic.
     */
    public void subscribeToStateEventsChannel() {
        try {
            this.jmsCommunicator.init("STATE_CHANNEL");
        } catch (JMSException jmsException) {
            LOGGER.error("JmsException while initializing JMS-Communicator: ", jmsException);
        }
    }

    private void loadAgentPresenceDb(List<MediaRoutingDomain> mediaRoutingDomains, List<CCUser> ccUsers) {
        List<AgentMrdState> agentMrdStates = this.getInitialAgentMrdStates(mediaRoutingDomains);
        Map<UUID, AgentPresence> agentPresenceMap = this.getAgentPresenceMap();
        Map<UUID, CCUser> ccUserMap = new HashMap<>();

        for (CCUser ccUser : ccUsers) {
            ccUserMap.put(ccUser.getId(), ccUser);
            if (!agentPresenceMap.containsKey(ccUser.getId())) {
                AgentPresence agentPresence = createAgentPresenceInstance(ccUser, agentMrdStates);
                // TODO: Add SaveALL feature to the redisDao
                agentPresenceMap.put(agentPresence.getAgent().getId(), agentPresence);
                this.agentPresenceRepository.save(agentPresence.getAgent().getId().toString(), agentPresence);
            }
        }

        agentPresenceMap.forEach((k, v) -> {
            if (!ccUserMap.containsKey(k)) {
                this.agentPresenceRepository.deleteById(k.toString());
            }
        });
    }

    private List<AgentMrdState> getInitialAgentMrdStates(List<MediaRoutingDomain> mediaRoutingDomains) {
        List<AgentMrdState> agentMrdStates = new ArrayList<>();
        for (MediaRoutingDomain mrd : mediaRoutingDomains) {
            agentMrdStates.add(new AgentMrdState(mrd, Enums.AgentMrdStateName.LOGOUT));
        }
        return agentMrdStates;
    }

    private Map<UUID, AgentPresence> getAgentPresenceMap() {
        Map<UUID, AgentPresence> agentPresenceMap = new HashMap<>();
        for (AgentPresence agentPresence : this.agentPresenceRepository.findAll()) {
            agentPresenceMap.put(agentPresence.getAgent().getId(), agentPresence);
        }
        return agentPresenceMap;
    }

    private AgentPresence createAgentPresenceInstance(CCUser ccUser, List<AgentMrdState> agentMrdStates) {
        AgentState state = new AgentState(Enums.AgentStateName.LOGOUT, null);
        return new AgentPresence(ccUser, state, agentMrdStates);
    }
}

