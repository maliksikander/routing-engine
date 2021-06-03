package com.ef.mediaroutingengine.services.utilities;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.repositories.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueEntityRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import com.ef.mediaroutingengine.services.redis.TaskDao;
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
    private final TaskDao taskDao;

    private final AgentsPool agentsPool;
    private final MrdPool mrdPool;
    private final PrecisionQueuesPool precisionQueuesPool;
    private final TasksPool tasksPool;

    private final JmsCommunicator jmsCommunicator;

    /**
     * Constructor. Loads the required beans.
     *
     * @param agentsRepository Agents config repository DAO
     * @param mediaRoutingDomainRepository Media-routing-domains config repository DAO
     * @param precisionQueueEntityRepository Precision-Queues config repository DAO
     * @param taskDao Tasks Repository DAO
     * @param agentsPool Agents Pool bean
     * @param mrdPool MRD Pool bean
     * @param precisionQueuesPool Precision-Queues Pool bean
     * @param tasksPool Tasks pool bean
     * @param jmsCommunicator JMS Communicator
     */
    @Autowired
    public BootUtility(AgentsRepository agentsRepository,
                       MediaRoutingDomainRepository mediaRoutingDomainRepository,
                       PrecisionQueueEntityRepository precisionQueueEntityRepository,
                       TaskDao taskDao,
                       AgentsPool agentsPool,
                       MrdPool mrdPool,
                       PrecisionQueuesPool precisionQueuesPool,
                       TasksPool tasksPool,
                       JmsCommunicator jmsCommunicator) {
        this.agentsRepository = agentsRepository;
        this.mediaRoutingDomainRepository = mediaRoutingDomainRepository;
        this.precisionQueueEntityRepository = precisionQueueEntityRepository;
        this.taskDao = taskDao;
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

        List<TaskDto> taskDtoList = this.taskDao.findAll();
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
            this.jmsCommunicator.init("STATE_CHANGE_CHANNEL");
        } catch (JMSException jmsException) {
            LOGGER.error("JmsException while initializing JMS-Communicator: ", jmsException);
        }
    }

}
