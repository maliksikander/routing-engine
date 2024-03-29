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
import com.ef.cim.objectmodel.task.TaskType;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.AgentRequestTimerService;
import com.ef.mediaroutingengine.routing.StepTimerService;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.AgentReqTimerEntity;
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
     * @param req   the request
     * @param mrdId the mrd id
     * @param queue the queue
     */
    public void enqueueTask(AssignResourceRequest req, String mrdId, PrecisionQueue queue) {
        String conversationId = req.getRequestSession().getConversationId();

        TaskMediaState mediaState = TaskMediaState.QUEUED;
        TaskMedia media = this.createMedia(req, UUID.randomUUID().toString(), mrdId, mediaState, queue.toTaskQueue());

        Task task = TaskUtility.createNewTask(conversationId, media, null);
        this.tasksRepository.insert(task);
        this.jmsCommunicator.publishTaskStateChanged(task, req.getRequestSession(), true, media.getId());
        this.agentRequestTimerService.start(task, media, queue.getId());
        logger.debug("Agent-Request-Ttl timer task scheduled");

        this.precisionQueuesPool.publishNewRequest(task, media);
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
            TaskMedia media = this.createMedia(req, task.getId(), mrdId, TaskMediaState.RESERVED, queue);

            if (agent.isAvailableForReservation(mrdId) && agent.reserveTask(task, media)) {
                task.addMedia(media);
                this.tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());

                if (req.isOfferToAgent()) {
                    restRequest.postAssignTask(task, media, media.getState(), agent.toCcUser(), true);
                }

                this.jmsCommunicator.publishTaskStateChanged(task, media.getRequestSession(), false, media.getId());
                this.jmsCommunicator.publishAgentReserved(task, media, agent.toCcUser());

                return true;
            }
        }

        return false;
    }

    /**
     * Create media task media.
     *
     * @param req    the req
     * @param taskId the task id
     * @param mrdId  the mrd id
     * @param state  the state
     * @param queue  the queue
     * @return the task media
     */
    private TaskMedia createMedia(AssignResourceRequest req, String taskId, String mrdId, TaskMediaState state,
                                  TaskQueue queue) {
        ChannelSession reqSession = req.getRequestSession();
        MrdType mrdType = this.mrdPool.getType(mrdId);
        List<ChannelSession> sessions = TaskUtility.getSessions(req.getChannelSessions(), reqSession, mrdId, mrdType);

        return new TaskMedia(mrdId, taskId, queue, req.getType(), req.getPriority(), state, reqSession, sessions);
    }

    /**
     * Load tasks on startup.
     *
     * @param tasks the tasks
     */
    public void loadTasksOnStartup(List<Task> tasks) {
        long queuedTasks = 0L;
        ListIterator<Task> itr = tasks.listIterator();

        while (itr.hasNext()) {
            Task task = itr.next();
            Agent agent = this.agentsPool.findBy(task.getAssignedTo());

            for (TaskMedia media : task.getActiveMedia()) {
                PrecisionQueue queue = this.precisionQueuesPool.findById(media.getQueue().getId());

                if (media.getState().equals(TaskMediaState.QUEUED)) {
                    if (this.enqueueTaskOnStartup(task, media, queue)) {
                        queuedTasks++;
                    } else {
                        itr.remove();
                    }

                    break;
                } else if (media.getState().equals(TaskMediaState.RESERVED)) {
                    this.agentRequestTimerService.startOnFailover(task, media, queue.getId());
                    agent.reserveTask(task, media);
                } else if (media.getState().equals(TaskMediaState.ACTIVE)) {
                    agent.addActiveTask(task, media);
                }
            }
        }

        if (queuedTasks > 0) {
            this.precisionQueuesPool.publishOnFailover();
        }
    }

    /**
     * Enqueue task on startup boolean.
     *
     * @param task  the task
     * @param media the media
     * @param queue the queue
     * @return the boolean
     */
    private boolean enqueueTaskOnStartup(Task task, TaskMedia media, PrecisionQueue queue) {
        if (this.agentRequestTimerService.startOnFailover(task, media, queue.getId())) {
            QueueTask queueTask = new QueueTask(task.getConversationId(), media);
            queue.enqueue(queueTask);
            this.stepTimerService.startNext(queueTask, queue, 0);

            logger.debug("Task: {} enqueued in Queue: {} on startup", task.getId(), queue.getId());
            return true;
        }

        this.closeTask(task, Enums.TaskStateReasonCode.NO_AGENT_AVAILABLE);
        return false;
    }

    /**
     * Close task.
     *
     * @param task       the task
     * @param reasonCode the reason code
     */
    public void closeTask(Task task, Enums.TaskStateReasonCode reasonCode) {
        this.closeTask(task, new TaskState(Enums.TaskStateName.CLOSED, reasonCode));
    }

    /**
     * Close task.
     *
     * @param task  the task
     * @param state the state
     */
    public void closeTask(Task task, TaskState state) {
        this.tasksRepository.delete(task);
        task.setState(state);

        Agent agent = this.agentsPool.findBy(task.getAssignedTo());
        String[] mediaStateChanges = this.closeTaskMedias(task, state, agent);

        ChannelSession session = task.getActiveMedia().get(task.getActiveMedia().size() - 1).getRequestSession();
        this.jmsCommunicator.publishTaskStateChanged(task, session, true, mediaStateChanges);
    }

    /**
     * Close task medias.
     *
     * @param task  the task
     * @param state the state
     * @param agent the agent
     * @return the string [ ]
     */
    private String[] closeTaskMedias(Task task, TaskState state, Agent agent) {
        String[] mediaStateChanges = new String[task.getActiveMedia().size()];
        int counter = 0;

        for (TaskMedia media : task.getActiveMedia()) {
            if (media.getState().equals(TaskMediaState.QUEUED)) {
                this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
                this.stepTimerService.stop(task.getId());
                this.precisionQueuesPool.findById(media.getQueue().getId()).removeTask(task.getId());
            } else if (media.getState().equals(TaskMediaState.RESERVED)) {
                if (!Enums.TaskStateReasonCode.RONA.equals(state.getReasonCode())) {
                    this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
                }
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

            mediaStateChanges[counter] = media.getId();
            counter++;
        }

        return mediaStateChanges;
    }

    /**
     * Reroute.
     *
     * @param task  the task
     * @param state the state
     */
    public void rerouteReserved(Task task, TaskState state) {
        String conversationId = task.getConversationId();
        TaskMedia reservedMedia = task.findMediaByState(TaskMediaState.RESERVED);

        if (!agentRequestTimerService.isRunning(task.getAgentRequestTtlTimerId())) {
            state.setReasonCode(Enums.TaskStateReasonCode.NO_AGENT_AVAILABLE);
            this.closeTask(task, state);
            this.jmsCommunicator.publishNoAgentAvailable(conversationId, reservedMedia);
        } else {
            Task newTask = Task.instanceOnReroute(task);
            this.closeTask(task, state);
            this.enqueueTaskOnReRoute(newTask);
        }
    }

    /**
     * Enqueue task on re route.
     *
     * @param task the task
     */
    private void enqueueTaskOnReRoute(Task task) {
        this.tasksRepository.insert(task);

        TaskMedia media = task.findMediaByState(TaskMediaState.QUEUED);

        AgentReqTimerEntity entity = new AgentReqTimerEntity(task.getId(), media.getId(), media.getQueue().getId());
        this.tasksRepository.saveAgentReqTimerEntity(task.getAgentRequestTtlTimerId(), entity);

        String[] mediaStateChanges = task.getActiveMedia().stream().map(TaskMedia::getId).toArray(String[]::new);
        ChannelSession session = media.getRequestSession();

        this.jmsCommunicator.publishTaskStateChanged(task, session, true, mediaStateChanges);
        this.precisionQueuesPool.publishNewRequest(task, media);
    }

    /**
     * Activate media.
     *
     * @param task  the task
     * @param media the media
     */
    public void activateMedia(Task task, TaskMedia media) {
        logger.debug("method started");
        Agent agent = this.agentsPool.findBy(task.getAssignedTo());

        List<String> mediaStateChanges = this.closeCurrentActive(agent, task, media);

        media.setState(TaskMediaState.ACTIVE);
        media.setAnswerTime(System.currentTimeMillis());

        mediaStateChanges.add(media.getId());

        ChannelSession session = media.getRequestSession();
        this.jmsCommunicator.publishTaskStateChanged(task, session, false, mediaStateChanges.toArray(new String[0]));

        task.getActiveMedia().removeIf(m -> m.getState().equals(TaskMediaState.CLOSED));
        this.tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());

        if (!this.mrdPool.getType(media.getMrdId()).isInterruptible()) {
            agent.setNonInterruptible(true);
        }

        agent.addActiveTask(task, media);
        logger.debug("Agent Task added in agent's active tasks");

        TaskType taskType = media.getType();
        logger.debug("Task type: {}", taskType);

        if (taskType.getMode().equals(Enums.TaskTypeMode.QUEUE) || TaskUtility.isNamedAgentTransfer(taskType)) {
            this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
            logger.debug("Agent Request Time stopped");

            agent.removeReservedTask();
            logger.debug("Agent reserved task removed");

            this.agentMrdStateListener.changeStateOnMediaActive(agent, media);
            logger.debug("Agent MRD State updated");

            if (agent.getNoOfActiveQueueTasks(media.getMrdId()) > 1) {
                this.precisionQueuesPool.publishRequestAccepted(media.getMrdId());
            }
        }

        logger.debug("method ended");
    }

    /**
     * Change current active to auto join.
     *
     * @param agent           the agent
     * @param task            the task
     * @param activatingMedia the activating media
     * @return the list
     */
    private List<String> closeCurrentActive(Agent agent, Task task, TaskMedia activatingMedia) {
        List<String> mediaStateChanges = new ArrayList<>();

        for (TaskMedia media : task.getActiveMedia()) {
            if (!media.getId().equals(activatingMedia.getId()) && media.getState().equals(TaskMediaState.ACTIVE)) {
                media.setState(TaskMediaState.CLOSED);
                mediaStateChanges.add(media.getId());

                agent.removeTask(task.getId(), media.getMrdId());
                this.agentMrdStateListener.changeStateOnMediaClose(agent, media);
            }
        }

        return mediaStateChanges;
    }

    /**
     * Revoke in process task boolean.
     *
     * @param task         the task
     * @param autoJoinAble the auto join able
     * @return the boolean
     */
    public boolean revokeInProcessTask(Task task, boolean autoJoinAble) {
        TaskMedia media = task.findInProcessMedia();

        if (media == null) {
            return false;
        }

        if (!autoJoinAble || this.mrdPool.getType(media.getMrdId()).isAutoJoin()) {
            this.revokeInProcessMedia(task, media);
            media.setState(TaskMediaState.CLOSED);

            ChannelSession session = media.getRequestSession();

            if (task.isRemovable()) {
                this.tasksRepository.delete(task);
                task.setState(new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.CANCELLED));
                jmsCommunicator.publishTaskStateChanged(task, session, true, media.getId());
                return true;
            } else {
                jmsCommunicator.publishTaskStateChanged(task, session, false, media.getId());
                task.removeMedia(media.getId());
                tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());
            }

        }

        return false;
    }

    /**
     * Revoke in process media.
     *
     * @param task  the task
     * @param media the media
     */
    private void revokeInProcessMedia(Task task, TaskMedia media) {
        if (media.getState().equals(TaskMediaState.QUEUED)) {
            PrecisionQueue queue = this.precisionQueuesPool.findById(media.getQueue().getId());

            this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
            this.stepTimerService.stop(task.getId());
            queue.removeTask(task.getId());
        } else if (media.getState().equals(TaskMediaState.RESERVED)) {
            this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
            this.agentsPool.findBy(task.getAssignedTo()).removeReservedTask();

            if (TaskUtility.getOfferToAgent(media)) {
                this.restRequest.postRevokeTask(task, true);
            }
        }
    }
}
