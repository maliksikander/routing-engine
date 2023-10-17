package com.ef.mediaroutingengine.routing;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
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
     * Instantiates a new Agent request timer service.
     *
     * @param stepTimerService the step timer service
     * @param jmsCommunicator  the jms communicator
     * @param tasksRepository  the tasks repository
     */
    @Autowired
    public AgentRequestTimerService(StepTimerService stepTimerService, JmsCommunicator jmsCommunicator,
                                    TasksRepository tasksRepository) {
        this.stepTimerService = stepTimerService;
        this.jmsCommunicator = jmsCommunicator;
        this.tasksRepository = tasksRepository;
    }

    /**
     * Start.
     *
     * @param task  the task
     * @param queue the queue
     */
    public void start(Task task, TaskMedia media, PrecisionQueue queue) {
        this.stop(task.getAgentRequestTtlTimerId());
        this.schedule(task.getAgentRequestTtlTimerId(), task.getId(), media.getId(),
                getDelay(media.getRequestSession()), queue);
    }

    /**
     * Start on failover.
     *
     * @param task  the task
     * @param queue the queue
     */
    public void startOnFailover(Task task, TaskMedia media, PrecisionQueue queue) {
        this.stop(task.getAgentRequestTtlTimerId());

        long delay = getDelay(media.getRequestSession()) - (System.currentTimeMillis() - media.getEnqueueTime());
        this.schedule(task.getAgentRequestTtlTimerId(), task.getId(), media.getId(), delay, queue);
    }

    /**
     * Schedule.
     *
     * @param timerId the task id
     * @param delay   the delay
     * @param queue   the queue
     */
    private void schedule(String timerId, String taskId, String mediaId, long delay, PrecisionQueue queue) {
        Timer timer = new Timer();
        this.timers.put(timerId, timer);
        timer.schedule(new AgentRequestTimerService.RequestTimerTask(timerId, taskId, mediaId, queue), delay);
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
        }
    }

    /**
     * The type Request timer task.
     */
    private class RequestTimerTask extends TimerTask {
        private final String timerId;
        private final String taskId;
        private final String mediaId;
        private final PrecisionQueue queue;

        /**
         * Instantiates a new Request ttl timer.
         *
         * @param taskId the task id
         * @param queue  the queue
         */
        public RequestTimerTask(String timerId, String taskId, String mediaId, PrecisionQueue queue) {
            this.timerId = timerId;
            this.taskId = taskId;
            this.mediaId = mediaId;
            this.queue = queue;
        }

        public void run() {
            logger.info("Agent Request TTL expired for request on task: {}", this.taskId);

            synchronized (queue.getServiceQueue()) {
                Task task = AgentRequestTimerService.this.tasksRepository.find(this.taskId);

                if (task == null) {
                    logger.error("Task not found, method returning...");
                    return;
                }

                TaskMedia media = task.findMediaBy(this.mediaId);

                if (media == null) {
                    logger.error("Task Media not found, method returning...");
                    return;
                }

                if (media.getState().equals(TaskMediaState.QUEUED)) {
                    handleQueued(task, media);
                } else if (media.getState().equals(TaskMediaState.RESERVED)) {
                    this.handleReserved(task, media);
                }
            }
        }

        private void handleQueued(Task task, TaskMedia media) {
            logger.info("In process task media: {} found in QUEUED state, removing task..", media.getId());

            AgentRequestTimerService.this.stepTimerService.stop(taskId);
            AgentRequestTimerService.this.stop(timerId);

            queue.removeByTaskId(taskId);
            AgentRequestTimerService.this.tasksRepository.deleteById(taskId);

            String conversationId = task.getConversationId();

            task.getActiveMedia().forEach(m -> {
                m.setState(TaskMediaState.CLOSED);
                AgentRequestTimerService.this.jmsCommunicator.publishTaskMediaStateChanged(conversationId, m);
            });

            task.setState(new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.NO_AGENT_AVAILABLE));

            AgentRequestTimerService.this.jmsCommunicator.publishTaskStateChanged(task, media.getRequestSession());
            AgentRequestTimerService.this.jmsCommunicator.publishNoAgentAvailable(conversationId, media);

            logger.info("Queued task: {} removed successfully", task.getId());

        }

        private void handleReserved(Task task, TaskMedia media) {
            media.setMarkedForDeletion(true);
            AgentRequestTimerService.this.tasksRepository.updateActiveMedias(taskId, task.getActiveMedia());
            logger.info("In process task media: {} found in Reserved state, task is marked for deletion", mediaId);
        }
    }
}
