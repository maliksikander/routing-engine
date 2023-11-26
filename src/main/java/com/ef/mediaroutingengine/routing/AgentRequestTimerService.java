package com.ef.mediaroutingengine.routing;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.locks.ConversationLock;
import com.ef.mediaroutingengine.routing.model.AgentReqTimerEntity;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Agent request timer service.
 */
@Service
public class AgentRequestTimerService {
    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(AgentRequestTimerService.class);
    /**
     * The Timers.
     */
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    /**
     * The Step timer service.
     */
    private final StepTimerService stepTimerService;
    /**
     * The Jms communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;

    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;

    /**
     * Instantiates a new Agent request timer service.
     *
     * @param stepTimerService    the step timer service
     * @param jmsCommunicator     the jms communicator
     * @param tasksRepository     the tasks repository
     * @param precisionQueuesPool the precision queues pool
     */
    @Autowired
    public AgentRequestTimerService(StepTimerService stepTimerService, JmsCommunicator jmsCommunicator,
                                    TasksRepository tasksRepository, PrecisionQueuesPool precisionQueuesPool) {
        this.stepTimerService = stepTimerService;
        this.jmsCommunicator = jmsCommunicator;
        this.tasksRepository = tasksRepository;
        this.precisionQueuesPool = precisionQueuesPool;
    }

    /**
     * Start.
     *
     * @param task    the task
     * @param media   the media
     * @param queueId the queue id
     */
    public void start(Task task, TaskMedia media, String queueId) {
        long delay = this.getDelay(media.getRequestSession());
        AgentReqTimerEntity entity = new AgentReqTimerEntity(task.getId(), media.getId(), queueId);

        this.schedule(task.getAgentRequestTtlTimerId(), task.getConversationId(), entity, delay);
    }

    /**
     * Start on failover.
     *
     * @param task    the task
     * @param media   the media
     * @param queueId the queue id
     */
    public void startOnFailover(Task task, TaskMedia media, String queueId) {
        long delay = getDelay(media.getRequestSession()) - (System.currentTimeMillis() - media.getEnqueueTime());
        AgentReqTimerEntity entity = new AgentReqTimerEntity(task.getId(), media.getId(), queueId);

        this.schedule(task.getAgentRequestTtlTimerId(), task.getConversationId(), entity, delay);
    }

    /**
     * Schedule.
     *
     * @param timerId the task id
     * @param entity  the entity
     * @param delay   the delay
     */
    private void schedule(String timerId, String conversationId, AgentReqTimerEntity entity, long delay) {
        logger.info("Request to schedule Agent Request Ttl timer initiated for timerId: {}", timerId);

        Timer timer = this.timers.get(timerId);
        if (timer != null) {
            logger.warn("Timer already running, returning...");
            return;
        }

        this.tasksRepository.saveAgentReqTimerEntity(timerId, entity);
        timer = new Timer();
        this.timers.put(timerId, timer);
        timer.schedule(new AgentRequestTimerService.RequestTimerTask(timerId, conversationId), delay);

        logger.info("Agent Request Ttl timer scheduled for {} ms, timerId: {}", delay, timerId);
    }

    /**
     * Gets delay.
     *
     * @param channelSession the channel session
     * @return the delay
     */
    private long getDelay(ChannelSession channelSession) {
        int ttl = channelSession.getChannel().getChannelConfig().getRoutingPolicy().getAgentRequestTtl();
        return ttl * 1000L;
    }

    /**
     * Stop.
     *
     * @param timerId the task id
     */
    public void stop(String timerId) {
        Timer timer = this.timers.get(timerId);

        if (timer != null) {
            timer.cancel();
            this.timers.remove(timerId);
            this.tasksRepository.deleteAgentReqTimerEntity(timerId);
        }
    }

    /**
     * The type Request timer task.
     */
    private class RequestTimerTask extends TimerTask {
        /**
         * The Timer id.
         */
        private final String timerId;
        private final String conversationId;
        private final ConversationLock conversationLock = new ConversationLock();

        /**
         * Instantiates a new Request ttl timer.
         *
         * @param timerId the timer id
         */
        public RequestTimerTask(String timerId, String conversationId) {
            this.timerId = timerId;
            this.conversationId = conversationId;
        }

        public void run() {
            logger.info("Agent Request TTL expired for timerId: {}, fetching timer entity...", this.timerId);

            AgentReqTimerEntity entity = AgentRequestTimerService.this.tasksRepository.getAgentReqTimerEntity(timerId);

            if (entity == null) {
                logger.warn("Timer entity is null for timerId: {}, returning...", this.timerId);
                return;
            }

            logger.info("Agent Request Timer Entity: {}", entity);

            PrecisionQueue queue = AgentRequestTimerService.this.precisionQueuesPool.findById(entity.getQueueId());

            if (queue == null) {
                logger.warn("Precision Queue not found for queueId: {}, returning...", entity.getQueueId());
                return;
            }

            try {
                conversationLock.lock(conversationId);

                Task task = AgentRequestTimerService.this.tasksRepository.find(entity.getTaskId());

                if (task == null) {
                    logger.error("Task not found for taskId: {}, returning...", entity.getTaskId());
                    return;
                }

                TaskMedia media = task.findMediaBy(entity.getMediaId());

                if (media == null) {
                    logger.error("Task Media not found for mediaId: {}, returning...", entity.getMediaId());
                    return;
                }

                if (media.getState().equals(TaskMediaState.QUEUED)) {
                    handleQueued(task, media, queue);
                } else if (media.getState().equals(TaskMediaState.RESERVED)) {
                    this.handleReserved(task, media);
                }

                AgentRequestTimerService.this.stop(this.timerId);
            } finally {
                conversationLock.unlock(conversationId);
            }
        }

        /**
         * Handle queued.
         *
         * @param task  the task
         * @param media the media
         * @param queue the queue
         */
        private void handleQueued(Task task, TaskMedia media, PrecisionQueue queue) {
            logger.info("In process task media: {} found in QUEUED state, removing task..", media.getId());

            AgentRequestTimerService.this.tasksRepository.delete(task);
            AgentRequestTimerService.this.stepTimerService.stop(task.getId());
            queue.removeTask(task.getId());

            task.setState(new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.NO_AGENT_AVAILABLE));
            task.getActiveMedia().forEach(m -> m.setState(TaskMediaState.CLOSED));

            String[] mediaChanges = task.getActiveMedia().stream().map(TaskMedia::getId).toArray(String[]::new);
            ChannelSession session = media.getRequestSession();

            AgentRequestTimerService.this.jmsCommunicator.publishTaskStateChanged(task, session, true, mediaChanges);
            AgentRequestTimerService.this.jmsCommunicator.publishNoAgentAvailable(session.getConversationId(), media);

            logger.info("Queued task: {} removed successfully", task.getId());
        }

        /**
         * Handle reserved.
         *
         * @param task  the task
         * @param media the media
         */
        private void handleReserved(Task task, TaskMedia media) {
            media.setMarkedForDeletion(true);
            AgentRequestTimerService.this.tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());
            logger.info("In process task media: {} in Reserved state, task is marked for deletion", media.getId());
        }
    }
}
