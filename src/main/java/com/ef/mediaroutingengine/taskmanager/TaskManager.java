package com.ef.mediaroutingengine.taskmanager;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MrdType;
import com.ef.cim.objectmodel.dto.AssignResourceRequest;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.cim.objectmodel.task.TaskQueue;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.AgentRequestTimerService;
import com.ef.mediaroutingengine.routing.StepTimerService;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.model.QueueTask;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.routing.utility.TaskUtility;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The type Task manager.
 */
@Service
public class TaskManager {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Mrd pool.
     */
    private final MrdPool mrdPool;
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    /**
     * The Step timer service.
     */
    private final StepTimerService stepTimerService;
    /**
     * The Agent request timer service.
     */
    private final AgentRequestTimerService agentRequestTimerService;
    /**
     * The Agent mrd state listener.
     */
    private final AgentMrdStateListener agentMrdStateListener;
    /**
     * The Rest request.
     */
    private final RestRequest restRequest;

    /**
     * Default Constructor. Loads the dependencies.
     *
     * @param tasksRepository          the tasks repository
     * @param precisionQueuesPool      the precision queues pool
     * @param agentsPool               the agents pool
     * @param mrdPool                  the mrd pool
     * @param jmsCommunicator          the jms communicator
     * @param stepTimerService         the step timer service
     * @param agentRequestTimerService the agent request timer service
     * @param agentMrdStateListener    the agent mrd state listener
     * @param restRequest              the rest request
     */
    @Autowired
    public TaskManager(TasksRepository tasksRepository,
                       PrecisionQueuesPool precisionQueuesPool, AgentsPool agentsPool,
                       MrdPool mrdPool, JmsCommunicator jmsCommunicator, StepTimerService stepTimerService,
                       AgentRequestTimerService agentRequestTimerService,
                       AgentMrdStateListener agentMrdStateListener, RestRequest restRequest) {
        this.tasksRepository = tasksRepository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.agentsPool = agentsPool;
        this.mrdPool = mrdPool;
        this.jmsCommunicator = jmsCommunicator;
        this.stepTimerService = stepTimerService;
        this.agentRequestTimerService = agentRequestTimerService;
        this.agentMrdStateListener = agentMrdStateListener;
        this.restRequest = restRequest;
    }

    /**
     * Enqueue task.
     *
     * @param request the request
     * @param queue   the queue
     */
    public void enqueueTask(AssignResourceRequest request, PrecisionQueue queue) {
        Task task = this.createTaskToEnqueue(request, queue.toTaskQueue());
        this.tasksRepository.save(task.getId(), task);
        // Publish Task State Changed event.

        TaskMedia queuedMedia = task.findMediaByState(TaskMediaState.QUEUED);

        this.agentRequestTimerService.start(task, queuedMedia, queue);
        logger.debug("Agent-Request-Ttl timer task scheduled");

        this.precisionQueuesPool.publishNewRequest(task, queuedMedia);
    }

    private Task createTaskToEnqueue(AssignResourceRequest req, TaskQueue taskQueue) {
        String mrdId = req.getRequestSession().getChannel().getChannelType().getMediaRoutingDomain();
        String conversationId = req.getRequestSession().getConversationId();
        String taskId = UUID.randomUUID().toString();

        TaskMedia queuedMedia = createMedia(req, taskId, mrdId, TaskMediaState.QUEUED, taskQueue);
        List<TaskMedia> medias = new ArrayList<>();
        medias.add(queuedMedia);

        TaskState state = new TaskState(Enums.TaskStateName.ACTIVE, null);
        String requestTimerId = UUID.randomUUID().toString();
        return new Task(taskId, conversationId, state, null, requestTimerId, medias);
    }

    /**
     * Reserve current available boolean.
     *
     * @param req   the request
     * @param tasks the tasks
     * @param mrdId the mrd id
     * @param queue the queue
     * @return the boolean
     */
    public boolean reserveCurrentAvailable(AssignResourceRequest req, List<Task> tasks, String mrdId,
                                           TaskQueue queue) {
        for (Task task : tasks) {
            Agent agent = this.agentsPool.findBy(task.getAssignedTo());

            if (agent == null) {
                continue;
            }

            if (agent.isAvailableForReservation(mrdId)) {
                TaskMedia media = this.createMedia(req, task.getId(), mrdId, TaskMediaState.RESERVED, queue);

                task.addMedia(media);
                this.tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());

                agent.reserveTask(task, media);

                if (req.isOfferToAgent()) {
                    restRequest.postAssignTask(task, media, media.getState(), agent.toCcUser(), true);
                }

                this.jmsCommunicator.publishTaskMediaStateChanged(task.getConversationId(), media);
                this.jmsCommunicator.publishAgentReserved(task, media, agent.toCcUser());

                return true;
            }
        }

        return false;
    }

    private TaskMedia createMedia(AssignResourceRequest req, String taskId, String mrdId, TaskMediaState state,
                                  TaskQueue queue) {
        List<ChannelSession> sessions = req.getChannelSessions().stream()
                .filter(c -> c.getChannel().getChannelType().getMediaRoutingDomain().equals(mrdId)).toList();

        return new TaskMedia(mrdId, taskId, queue, req.getType(), req.getPriority(), state,
                req.getRequestSession(), sessions);
    }

    /**
     * Enqueues task all present in the redis DB at start of application.
     *
     * @param tasks the tasks
     */
    public void enqueueQueuedTasksOnFailover(List<Task> tasks) {
        long queuedTasks = 0L;
        for (Task task : tasks) {

            TaskMedia queuedMedia = task.findMediaByState(TaskMediaState.QUEUED);

            if (queuedMedia == null) {
                continue;
            }

            PrecisionQueue queue = this.precisionQueuesPool.findById(queuedMedia.getQueue().getId());
            if (queue != null) {
                this.agentRequestTimerService.startOnFailover(task, queuedMedia, queue);

                QueueTask queueTask = new QueueTask(task.getConversationId(), queuedMedia);
                queue.enqueue(queueTask);
                logger.debug("Task: {} enqueued in Precision-Queue: {}", task.getId(), queue.getId());

                this.stepTimerService.startNext(queueTask, queue, 0);
                queuedTasks++;
            }
        }

        if (queuedTasks > 0) {
            this.precisionQueuesPool.publishOnFailover();
        }
    }

    /**
     * Close task.
     *
     * @param task  the task
     * @param state the state
     */
    public void closeTask(Task task, TaskState state) {
        this.tasksRepository.deleteById(task.getId());
        task.setState(state);

        Agent agent = this.agentsPool.findBy(task.getAssignedTo());
        ChannelSession session = task.getActiveMedia().get(0).getRequestSession();
        this.closeTaskMedias(task, agent);

        this.jmsCommunicator.publishTaskStateChanged(task, session);
    }

    /**
     * Close task medias.
     *
     * @param task  the task
     * @param agent the agent
     */
    private void closeTaskMedias(Task task, Agent agent) {
        ListIterator<TaskMedia> itr = task.getActiveMedia().listIterator();

        while (itr.hasNext()) {
            TaskMedia media = itr.next();

            if (media.getState().equals(TaskMediaState.QUEUED)) {
                this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
                this.stepTimerService.stop(task.getId());
                this.precisionQueuesPool.findById(media.getQueue().getId()).removeByTaskId(task.getId());

            } else if (media.getState().equals(TaskMediaState.RESERVED)) {
                this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
                agent.removeReservedTask();

            } else if (media.getState().equals(TaskMediaState.ACTIVE)) {
                agent.removeTask(task.getId(), media.getMrdId());

                MrdType mrdType = this.mrdPool.getType(media.getMrdId());
                if (!mrdType.isInterruptible()) {
                    agent.setNonInterruptible(false);
                }

                this.agentMrdStateListener.changeStateOnMediaClose(agent, media);
            }

            media.setState(TaskMediaState.CLOSED);
            this.jmsCommunicator.publishTaskMediaStateChanged(task.getConversationId(), media);
            itr.remove();
        }
    }

    /**
     * Reroute.
     *
     * @param task  the task
     * @param state the state
     */
    public void reroute(Task task, TaskState state) {
        String conversationId = task.getConversationId();
        TaskMedia reservedMedia = task.findMediaByState(TaskMediaState.RESERVED);

        if (reservedMedia.isMarkedForDeletion()) {
            this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
            this.closeTask(task, state);
            this.jmsCommunicator.publishNoAgentAvailable(conversationId, reservedMedia);
        } else {
            Task newTask = Task.instanceOnReroute(task);
            this.closeTask(task, state);

            this.tasksRepository.save(newTask.getId(), newTask);

            newTask.getActiveMedia().forEach(m -> jmsCommunicator.publishTaskMediaStateChanged(conversationId, m));
            this.jmsCommunicator.publishTaskStateChanged(newTask, reservedMedia.getRequestSession());

            TaskMedia queuedMedia = newTask.findMediaByState(TaskMediaState.QUEUED);
            this.precisionQueuesPool.publishNewRequest(newTask, queuedMedia);
        }
    }

    /**
     * Activate media.
     *
     * @param task  the task
     * @param media the media
     */
    public void activateMedia(Task task, TaskMedia media) {
        Agent agent = this.agentsPool.findBy(task.getAssignedTo());
        this.closeCurrentActive(agent, task, media);

        media.setState(TaskMediaState.ACTIVE);
        this.jmsCommunicator.publishTaskMediaStateChanged(task.getConversationId(), media);

        this.tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());

        if (!this.mrdPool.getType(media.getMrdId()).isInterruptible()) {
            agent.setNonInterruptible(true);
        }

        agent.addActiveTask(task, media);

        if (media.getType().getMode().equals(Enums.TaskTypeMode.QUEUE)) {
            this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
            agent.removeReservedTask();

            this.agentMrdStateListener.changeStateOnMediaActive(agent, media);

            if (agent.getNoOfActiveQueueTasks(media.getMrdId()) > 1) {
                this.precisionQueuesPool.publishRequestAccepted(media.getMrdId());
            }
        }
    }

    /**
     * Change current active to auto join.
     *
     * @param agent           the agent
     * @param task            the task
     * @param activatingMedia the activating media
     */
    private void closeCurrentActive(Agent agent, Task task, TaskMedia activatingMedia) {
        ListIterator<TaskMedia> itr = task.getActiveMedia().listIterator();

        while (itr.hasNext()) {
            TaskMedia media = itr.next();
            if (!media.getId().equals(activatingMedia.getId()) && media.getState().equals(TaskMediaState.ACTIVE)) {
                media.setState(TaskMediaState.CLOSED);
                this.jmsCommunicator.publishTaskMediaStateChanged(task.getConversationId(), media);

                itr.remove();

                agent.removeTask(task.getId(), media.getMrdId());
                this.agentMrdStateListener.changeStateOnMediaClose(agent, media);
            }
        }
    }

    /**
     * Revoke in process task boolean.
     *
     * @param task the task
     * @return the boolean
     */
    public boolean revokeInProcessTask(Task task) {
        ListIterator<TaskMedia> itr = task.getActiveMedia().listIterator();

        ChannelSession channelSession = null;
        while (itr.hasNext()) {
            TaskMedia media = itr.next();

            if (this.mrdPool.getType(media.getMrdId()).isAutoJoin()) {
                if (media.getState().equals(TaskMediaState.QUEUED)) {
                    this.revokeQueuedMedia(task, media);
                    itr.remove();
                    channelSession = media.getRequestSession();
                } else if (media.getState().equals(TaskMediaState.RESERVED)) {
                    this.revokeReservedMedia(task, media);
                    itr.remove();
                    channelSession = media.getRequestSession();
                }
            }
        }

        if (task.isRemovable()) {
            this.tasksRepository.deleteById(task.getId());
            task.setState(new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.CANCELLED));
            this.jmsCommunicator.publishTaskStateChanged(task, channelSession);
            return true;
        }

        return false;
    }

    /**
     * Revoke queued media.
     *
     * @param task  the task
     * @param media the media
     */
    public void revokeQueuedMedia(Task task, TaskMedia media) {
        this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
        this.stepTimerService.stop(task.getId());
        this.precisionQueuesPool.findById(media.getQueue().getId()).removeByTaskId(task.getId());
        media.setState(TaskMediaState.CLOSED);
        this.jmsCommunicator.publishTaskMediaStateChanged(task.getConversationId(), media);
    }

    /**
     * Revoke reserved media.
     *
     * @param task  the task
     * @param media the media
     */
    public void revokeReservedMedia(Task task, TaskMedia media) {
        this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
        Agent agent = this.agentsPool.findBy(task.getAssignedTo());
        agent.removeReservedTask();
        if (TaskUtility.getOfferToAgent(media)) {
            this.restRequest.postRevokeTask(task, true);
        }
        media.setState(TaskMediaState.CLOSED);
        this.jmsCommunicator.publishTaskMediaStateChanged(task.getConversationId(), media);
    }
}
