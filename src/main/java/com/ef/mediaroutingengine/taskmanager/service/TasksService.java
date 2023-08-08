package com.ef.mediaroutingengine.taskmanager.service;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.dto.QueueHistoricalStats;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.dto.TaskEwtAndPositionResponse;
import com.ef.mediaroutingengine.taskmanager.dto.UpdateTaskRequest;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import com.ef.mediaroutingengine.taskmanager.service.taskservice.TasksRetriever;
import com.ef.mediaroutingengine.taskmanager.service.taskservice.TasksRetrieverFactory;
import java.util.ArrayList;
import java.util.List;
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
    public ResponseEntity<Object> getTaskEwtAndPosition(String conversationId) {
        logger.info("Request received to fetch the EWT and position for conversation id: {}", conversationId);
        try {
            List<Task> queuedTasks = this.tasksPool.findQueuedTasksFor(conversationId);
            List<TaskEwtAndPositionResponse> responseList = new ArrayList<>();

            if (queuedTasks.isEmpty()) {
                logger.info("No queued tasks found for conversation id: {}", conversationId);
                return new ResponseEntity<>(responseList, HttpStatus.OK);
            }

            logger.info("{} task found in queue for conversation id: {}", queuedTasks.size(), conversationId);

            for (Task task : queuedTasks) {
                TaskEwtAndPositionResponse response = calculateTaskEwtAndPosition(task);
                logger.info("EWT and position for task {} : {}", task.getId(), response);
                responseList.add(response);
            }

            return new ResponseEntity<>(responseList, HttpStatus.OK);

        } catch (Exception ex) {
            String errorMessage = "Internal processing exception occurred: " + ex.getMessage();
            logger.error(errorMessage, ex);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns the EWT and position of a task.
     *
     * @param task The task.
     * @return the task, its EWT, and position.
     */
    public TaskEwtAndPositionResponse calculateTaskEwtAndPosition(Task task) {
        int ewt;
        int associatedAgentsCount;
        int queuePosition = getTaskPosition(task);

        logger.info("Request received to fetch the EWT and position for conversation id: {}", task.getId());

        PrecisionQueue precisionQueue = queuesPool.findById(task.getQueue().getId());

        QueueHistoricalStats queueHistoricalStats = restRequest.getQueueHistoricalStats(task.getQueue().getId());

        if (queueHistoricalStats.getAverageHandleTime() == 0) {
            queueHistoricalStats.setAverageHandleTime(5);
        }

        associatedAgentsCount = precisionQueue.getAssociatedAgents().isEmpty() ? 1 : precisionQueue
                .getAssociatedAgents().size();

        ewt = queuePosition * queueHistoricalStats.getAverageHandleTime() / associatedAgentsCount;

        return new TaskEwtAndPositionResponse(AdapterUtility.createTaskDtoFrom(task), ewt, queuePosition);
    }

    /**
     * get task position.
     *
     * @param task the task
     * @return the task position
     */
    public int getTaskPosition(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task object is null");
        }
        int priority = task.getPriority();
        long enqueueTime = task.getEnqueueTime();
        String queueId = task.getQueue().getId();
        PrecisionQueue precisionQueue = queuesPool.findById(queueId);
        List<Task> tasks = precisionQueue.getTasks();
        List<Task> filteredTasks = tasks.stream()
                .filter(t -> (t.getPriority() > priority || (t.getPriority() == priority
                        && t.getEnqueueTime() < enqueueTime)))
                .toList();
        return filteredTasks.size() + 1;
    }
}
