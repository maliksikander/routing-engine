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
    private final Logger logger = LoggerFactory.getLogger(PullAssignTaskServiceImpl.class);
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
        logger.info("Request to assign PULL task to agent: {} initiated", agent.getId());

        Task task = Task.getInstanceFrom(agent.getId(), mrd, channelSession);
        logger.debug("Task {} created with state: {}", task.getId(), task.getTaskState());

        this.tasksPool.add(task);
        logger.debug("Task {} added in in-memory Tasks pool", task.getId());

        agent.addActiveTask(task);
        logger.debug("Task {} added in agent's active-tasks list", task.getId());

        TaskDto taskDto = new TaskDto(task);
        this.tasksRepository.save(taskDto.getId().toString(), taskDto);
        logger.debug("Task {} inserted in Tasks repository", task.getId());

        this.jmsCommunicator.publishTaskStateChangeForReporting(task);
        logger.debug("Task {} published for reporting", task.getId());

        logger.info("PULL task: {} assigned to agent: {} successfully", task.getId(), agent.getId());
        return taskDto;
    }
}
