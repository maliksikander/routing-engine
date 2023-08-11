package com.ef.mediaroutingengine.taskmanager.service;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.QueueHistoricalStatsDto;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.dto.TaskEwtResponse;
import com.ef.mediaroutingengine.taskmanager.dto.UpdateTaskRequest;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import com.ef.mediaroutingengine.taskmanager.service.taskservice.TasksRetriever;
import com.ef.mediaroutingengine.taskmanager.service.taskservice.TasksRetrieverFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * The type Tasks service.
 */
@Service
public class TasksService {
    private static final Logger logger = LoggerFactory.getLogger(TasksService.class);
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    private final PrecisionQueuesPool queuesPool;
    private final RestRequest restRequest;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Task retriever factory.
     */
    private final TasksRetrieverFactory tasksRetrieverFactory;
    /**
     * JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Instantiates a new Tasks service.
     *
     * @param tasksPool             the tasks pool
     * @param tasksRetrieverFactory the task retriever factory
     */
    @Autowired
    public TasksService(TasksPool tasksPool, PrecisionQueuesPool queuesPool, TasksRepository tasksRepository,
                        TasksRetrieverFactory tasksRetrieverFactory, JmsCommunicator jmsCommunicator,
                        RestRequest restRequest) {
        this.tasksPool = tasksPool;
        this.queuesPool = queuesPool;
        this.tasksRepository = tasksRepository;
        this.tasksRetrieverFactory = tasksRetrieverFactory;
        this.jmsCommunicator = jmsCommunicator;
        this.restRequest = restRequest;
    }

    /**
     * Retrieve by id task dto.
     *
     * @param taskId the task id
     * @return the task dto
     */
    public TaskDto retrieveById(String taskId) {
        Task task = this.tasksPool.findById(taskId);
        if (task != null) {
            return AdapterUtility.createTaskDtoFrom(task);
        } else {
            throw new NotFoundException("Task not found in Task pool");
        }
    }

    /**
     * Retrieve list.
     *
     * @param agentId   the agent id
     * @param taskState the task state
     * @return the list
     */
    public List<TaskDto> retrieve(Optional<String> agentId,
                                  Optional<Enums.TaskStateName> taskState) {
        TasksRetriever tasksRetriever = this.tasksRetrieverFactory.getRetriever(agentId, taskState);
        return tasksRetriever.findTasks();
    }

    /**
     * Update task task dto.
     *
     * @param taskId  the task id
     * @param reqBody the req body
     * @return the task dto
     */
    public TaskDto updateTask(String taskId, UpdateTaskRequest reqBody) {
        Task task = this.tasksPool.findById(taskId);
        if (task == null) {
            throw new NotFoundException("Task not found in Task pool");
        }

        task.setChannelSession(reqBody.getChannelSession());
        this.tasksRepository.updateChannelSession(taskId, reqBody.getChannelSession());
        jmsCommunicator.publishTaskStateChangeForReporting(task);
        return AdapterUtility.createTaskDtoFrom(task);
    }

    /**
     * Calls the required methods for EWT and position.
     *
     * @param conversationId The conversation id.
     * @return The response entity.
     */
    public ResponseEntity<Object> getEwtAndPosition(String conversationId) {
        logger.info("Request received to fetch the EWT and position for conversation id: {}", conversationId);

        List<TaskEwtResponse> responses = new ArrayList<>();
        Map<String, List<Task>> queuedTasks = this.tasksPool.findQueuedGroupedByQueueId(conversationId);

        for (Map.Entry<String, List<Task>> entry : queuedTasks.entrySet()) {
            QueueHistoricalStatsDto queueStats = this.restRequest.getQueueHistoricalStats(entry.getKey());

            PrecisionQueue precisionQueue = queuesPool.findById(entry.getKey());
            int totalAgents = precisionQueue.getAssociatedAgents().size();

            synchronized (precisionQueue.getServiceQueue()) {
                for (Task task : entry.getValue()) {
                    int position = precisionQueue.getPosition(task);

                    if (position == -1) {
                        continue;
                    }

                    int ewt = this.calculateEwt(position, totalAgents, queueStats.getAverageHandleTime());
                    responses.add(new TaskEwtResponse(AdapterUtility.createTaskDtoFrom(task), ewt, position));
                }
            }
        }

        logger.info("Request to fetch the EWT and position for conversation id: {} handled", conversationId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    private int calculateEwt(int taskPosition, int totalAgents, int averageHandleTime) {
        return totalAgents == 0 ? Integer.MAX_VALUE : taskPosition * averageHandleTime / totalAgents;
    }
}