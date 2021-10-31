package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Pull assign task service.
 */
@Service
public class PullAssignTaskServiceImpl implements PullAssignTaskService {
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
        Task task = Task.getInstance(agent.getId(), mrd, channelSession);
        this.tasksPool.add(task);
        agent.addActiveTask(task);
        TaskDto taskDto = new TaskDto(task);
        this.tasksRepository.save(taskDto.getId().toString(), taskDto);
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);
        return taskDto;
    }
}
