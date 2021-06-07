package com.ef.mediaroutingengine.services.utilities;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.MrdState;
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
import java.util.List;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BootUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootUtility.class);

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
    public BootUtility(AgentsRepository agentsRepository,
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

        List<PrecisionQueueEntity> precisionQueueEntities = precisionQueueEntityRepository.findAll();
        this.precisionQueuesPool.loadPoolFrom(precisionQueueEntities);

        List<TaskDto> taskDtoList = this.tasksRepository.findAll();
        for (TaskDto taskDto : taskDtoList) {
            this.tasksPool.enqueueTask(new Task(taskDto));
        }

        this.loadAgentPresenceDb(mediaRoutingDomains, ccUsers);

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
            this.jmsCommunicator.init("STATE_CHANGE_CHANNEL");
        } catch (JMSException jmsException) {
            LOGGER.error("JmsException while initializing JMS-Communicator: ", jmsException);
        }
    }

    private void loadAgentPresenceDb(List<MediaRoutingDomain> mediaRoutingDomains, List<CCUser> ccUsers) {
        List<AgentMrdState> agentMrdStates = this.getInitialAgentMrdStates(mediaRoutingDomains);
        List<AgentPresence> agentPresenceList = this.agentPresenceRepository.findAll();

        for (CCUser ccUser : ccUsers) {
            if (!agentExistsInAgentPresenceDb(ccUser, agentPresenceList)) {
                AgentPresence agentPresence = createAgentPresenceInstance(ccUser, agentMrdStates);
                // TODO: Add SaveALL feature to the redisDao
                this.agentPresenceRepository.save(agentPresence.getAgent().getId().toString(), agentPresence);
            }
        }
    }

    private List<AgentMrdState> getInitialAgentMrdStates(List<MediaRoutingDomain> mediaRoutingDomains) {
        List<AgentMrdState> agentMrdStates = new ArrayList<>();
        for (MediaRoutingDomain mrd : mediaRoutingDomains) {
            MrdState state = new MrdState(Enums.AgentMrdStateName.NOT_READY, Enums.AgentMrdStateReasonCode.NONE);
            agentMrdStates.add(new AgentMrdState(mrd.getId(), state));
        }
        return agentMrdStates;
    }

    private boolean agentExistsInAgentPresenceDb(CCUser ccUser, List<AgentPresence> agentPresenceList) {
        for (AgentPresence agentPresence : agentPresenceList) {
            if (ccUser.equals(agentPresence.getAgent())) {
                return true;
            }
        }
        return false;
    }

    private AgentPresence createAgentPresenceInstance(CCUser ccUser, List<AgentMrdState> agentMrdStates) {
        AgentState state = new AgentState(Enums.AgentStateName.LOGOUT, Enums.AgentStateReasonCode.NONE);
        return new AgentPresence(ccUser, state, agentMrdStates);
    }

}
