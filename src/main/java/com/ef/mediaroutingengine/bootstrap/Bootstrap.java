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
        List<CCUser> ccUsers = agentsRepository.findAll();
        this.agentsPool.loadPoolFrom(ccUsers);

        List<MediaRoutingDomain> mediaRoutingDomains = mediaRoutingDomainRepository.findAll();
        this.mrdPool.loadPoolFrom(mediaRoutingDomains);

        // Update Agent and AgentMRD states after MRD pool is loaded as it is required for agent-mrd states.
        this.updateAgentStates();

        // Load Precision-Queue pool. Requires Agents pool to be loaded first.
        List<PrecisionQueueEntity> precisionQueueEntities = precisionQueueEntityRepository.findAll();
        this.precisionQueuesPool.loadPoolFrom(precisionQueueEntities);

        List<TaskDto> taskDtoList = this.tasksRepository.findAll();
        for (TaskDto taskDto : taskDtoList) {
            Task task = new Task(taskDto);
            Agent agent = this.agentsPool.findById(task.getAssignedTo());
            if (agent != null) {
                if (task.getTaskState().getName().equals(Enums.TaskStateName.RESERVED)) {
                    agent.reserveTask(task);
                } else if (task.getTaskState().getName().equals(Enums.TaskStateName.ACTIVE)) {
                    agent.addActiveTask(task);
                }
            }
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

    private List<AgentMrdState> getInitialAgentMrdStates(List<MediaRoutingDomain> mediaRoutingDomains) {
        List<AgentMrdState> agentMrdStates = new ArrayList<>();
        for (MediaRoutingDomain mrd : mediaRoutingDomains) {
            agentMrdStates.add(new AgentMrdState(mrd, Enums.AgentMrdStateName.NOT_READY));
        }
        return agentMrdStates;
    }

    private Map<UUID, AgentPresence> getCurrentAgentPresenceMap() {
        List<AgentPresence> currentAgentPresenceList = this.agentPresenceRepository.findAll();
        for (AgentPresence agentPresence : currentAgentPresenceList) {
            this.agentPresenceRepository.deleteById(agentPresence.getAgent().getId().toString());
        }
        Map<UUID, AgentPresence> currentAgentPresenceMap = new HashMap<>();
        for (AgentPresence agentPresence : currentAgentPresenceList) {
            currentAgentPresenceMap.put(agentPresence.getAgent().getId(), agentPresence);
        }
        return currentAgentPresenceMap;
    }

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

    private void updateAgentStates() {
        Map<UUID, AgentPresence> currentAgentPresenceMap = this.getCurrentAgentPresenceMap();
        List<AgentPresence> updatedAgentPresenceList = new ArrayList<>();
        for (Agent agent : this.agentsPool.toList()) {
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
            updatedAgentPresenceList.add(agentPresence);
        }
        for (AgentPresence agentPresence : updatedAgentPresenceList) {
            this.agentPresenceRepository.save(agentPresence.getAgent().getId().toString(), agentPresence);
        }
    }
}

