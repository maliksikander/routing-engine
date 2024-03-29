package com.ef.mediaroutingengine.routing;

import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.locks.ConversationLock;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.AgentSelectionCriteria;
import com.ef.mediaroutingengine.routing.model.NewTaskPayload;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.model.QueueEventName;
import com.ef.mediaroutingengine.routing.model.QueueTask;
import com.ef.mediaroutingengine.routing.model.Step;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.routing.utility.TaskUtility;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Task scheduler.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TaskRouter implements PropertyChangeListener {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskRouter.class);
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Jms communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    /**
     * The Precision queue.
     */
    private PrecisionQueue precisionQueue;
    /**
     * The Is init.
     */
    private boolean isInit;
    /**
     * The Rest Request Class Object.
     */
    private final RestRequest restRequest;
    /**
     * The Step timer service.
     */
    private final StepTimerService stepTimerService;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    private final ConversationLock conversationLock = new ConversationLock();

    /**
     * Constructor.
     *
     * @param agentsPool       the pool of all agents
     * @param jmsCommunicator  the jms communicator
     * @param restRequest      the rest request
     * @param stepTimerService the step timer service
     * @param tasksRepository  the tasks repo
     */
    @Autowired
    public TaskRouter(AgentsPool agentsPool, JmsCommunicator jmsCommunicator, RestRequest restRequest,
                      StepTimerService stepTimerService, TasksRepository tasksRepository) {
        this.agentsPool = agentsPool;
        this.jmsCommunicator = jmsCommunicator;
        this.restRequest = restRequest;
        this.stepTimerService = stepTimerService;
        this.tasksRepository = tasksRepository;
    }

    /**
     * Initializes the Scheduler.
     *
     * @param precisionQueue precision queue associated with this scheduler
     */
    public void init(PrecisionQueue precisionQueue) {
        if (!isInit) {
            this.precisionQueue = precisionQueue;
            this.isInit = true;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String evtName = evt.getPropertyName();
        logger.debug("TaskRouter for queue: [{}] invoked on event [{}]", precisionQueue.getName(), evtName);

        if (QueueEventName.NEW_REQUEST.equals(evtName)) {
            this.onNewRequest(evt);
        }

        try {
            synchronized (precisionQueue.getServiceQueue()) {
                QueueTask queueTask = precisionQueue.peek();

                if (queueTask == null) {
                    logger.debug("Queue [{}] is empty", this.precisionQueue.getName());
                    return;
                }

                logger.debug("Queue [{}] is not empty", this.precisionQueue.getName());

                try {
                    conversationLock.lock(queueTask.getConversationId());

                    Task task = this.tasksRepository.find(queueTask.getTaskId());
                    if (task == null || task.findMediaBy(queueTask.getMediaId()) == null) {
                        precisionQueue.dequeue();
                        return;
                    }

                    reserve(queueTask, task);
                } finally {
                    conversationLock.unlock(queueTask.getConversationId());
                }
            }
        } catch (Exception ex) {
            logger.error(ExceptionUtils.getMessage(ex));
            logger.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * Listen to task property changes if new task event fired.
     *
     * @param evt the evt
     */
    private void onNewRequest(PropertyChangeEvent evt) {
        NewTaskPayload payload = (NewTaskPayload) evt.getNewValue();
        Task task = payload.task();
        TaskMedia media = payload.media();

        QueueTask queueTask = new QueueTask(task.getConversationId(), media);

        if (queueTask.getQueueId().equals(this.precisionQueue.getId())) {
            this.precisionQueue.enqueue(queueTask);

            jmsCommunicator.publishTaskEnqueued(task, media, this.precisionQueue);
            logger.debug("Task: {} enqueued in Precision-Queue: {}", queueTask.getId(), precisionQueue.getId());
            this.stepTimerService.startNext(queueTask, this.precisionQueue, 0);
        }
    }

    /**
     * Reserve.
     *
     * @param queueTask the queue task
     * @param task      the task
     */
    private void reserve(QueueTask queueTask, Task task) {
        TaskMedia media = task.findMediaBy(queueTask.getMediaId());

        boolean assignedToLastAssignedAgent = this.assignToLastAssignedAgent(task, media);

        if (!assignedToLastAssignedAgent) {
            int currentStepIndex = precisionQueue.getStepIndex(queueTask.getCurrentStep().getStep());

            for (int i = 0; i < currentStepIndex + 1; i++) {
                Step step = precisionQueue.getStepAt(i);
                logger.info("Step: {} searching in queue: {}", i, precisionQueue.getName());
                Agent agent = this.getAvailableAgentWithLeastActiveTasks(step, queueTask.getConversationId());
                if (agent != null) {
                    logger.debug("Agent: {} is available to schedule queueTask: {}", agent.getId(), queueTask.getId());
                    this.assignTaskTo(agent, task, media);
                    return;
                }
            }

            logger.debug("Could not find an agent at the moment for queueTask: {}", queueTask.getId());
        }
    }

    /**
     * Assign to last assigned agent boolean.
     *
     * @param task  the task
     * @param media the media
     * @return the boolean
     */
    private boolean assignToLastAssignedAgent(Task task, TaskMedia media) {
        String lastAssignedAgentId = TaskUtility.getLastAssignedAgentId(media);
        logger.info("Last assigned agent-id {} ", lastAssignedAgentId);

        if (lastAssignedAgentId != null) {
            Agent agent = this.agentsPool.findBy(lastAssignedAgentId);

            if (agent != null && agent.isAvailableForReservation(media.getMrdId(), task.getConversationId())) {
                assignTaskTo(agent, task, media);
                return true;
            }
        }

        return false;
    }

    /**
     * Gets available agent with the least number of active tasks.
     *
     * @param step           the step
     * @param conversationId the conversation id
     * @return the available agent with the least number of active tasks
     */
    Agent getAvailableAgentWithLeastActiveTasks(Step step, String conversationId) {
        String mrdId = this.precisionQueue.getMrd().getId();
        List<Agent> sortedAgentList = step.orderAgentsBy(AgentSelectionCriteria.LONGEST_AVAILABLE, mrdId);
        int lowestNumberOfTasks = Integer.MAX_VALUE;
        Agent result = null;

        for (Agent agent : sortedAgentList) {
            int noOfTasksOnMrd = agent.getNoOfActiveQueueTasks(mrdId);

            if (agent.isAvailableForReservation(mrdId, conversationId) && noOfTasksOnMrd < lowestNumberOfTasks) {
                lowestNumberOfTasks = noOfTasksOnMrd;
                result = agent;
            }
        }
        return result;
    }

    /**
     * Assign queueTask to.
     *
     * @param agent the agent
     * @param task  the task
     * @param media the media
     */
    private void assignTaskTo(Agent agent, Task task, TaskMedia media) {
        try {
            boolean isReserved = agent.reserveTask(task, media);

            if (!isReserved) {
                return;
            }

            if (this.offerToAgent(task, media, agent)) {
                task.setAssignedTo(agent.toTaskAgent());
                media.setState(TaskMediaState.RESERVED);
                this.tasksRepository.update(task);

                this.stepTimerService.stop(task.getId());
                this.precisionQueue.dequeue();

                this.jmsCommunicator.publishTaskStateChanged(task, media.getRequestSession(), false, media.getId());
                this.jmsCommunicator.publishAgentReserved(task, media, agent.toCcUser());
            } else {
                agent.removeReservedTask();
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Present task boolean.
     *
     * @param task  the task
     * @param media the media
     * @param agent the agent
     * @return the boolean
     */
    private boolean offerToAgent(Task task, TaskMedia media, Agent agent) {
        if (TaskUtility.getOfferToAgent(media)) {
            return this.restRequest.postAssignTask(task, media, TaskMediaState.RESERVED, agent.toCcUser(), false);
        }
        return true;
    }
}