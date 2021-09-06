package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Pull assign task service.
 */
@Service
public class PullAssignTaskServiceImpl implements PullAssignTaskService {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PullAssignTaskServiceImpl.class);

    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    /**
     * The Jms communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Instantiates a new Pull assign task service.
     *
     * @param tasksRepository the tasks repository
     * @param tasksPool       the tasks pool
     * @param jmsCommunicator the jms communicator
     */
    @Autowired
    public PullAssignTaskServiceImpl(TasksRepository tasksRepository,
                                     TasksPool tasksPool, JmsCommunicator jmsCommunicator) {
        this.tasksRepository = tasksRepository;
        this.tasksPool = tasksPool;
        this.jmsCommunicator = jmsCommunicator;
    }

    @Override
    public TaskDto assignTask(Agent agent, MediaRoutingDomain mrd, ChannelSession channelSession) {
        System.out.println("S1");
        Task task = new Task(agent.getId(), mrd, channelSession);
        System.out.println("S1");
        this.tasksPool.add(task);
        System.out.println("S2");
        agent.addActiveTask(task);
        System.out.println("S3");
        TaskDto taskDto = new TaskDto(task);
        System.out.println("S4");
        this.tasksRepository.save(taskDto.getId().toString(), taskDto);
        System.out.println("S5");
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);
        System.out.println("S6");
        return taskDto;
    }
}
