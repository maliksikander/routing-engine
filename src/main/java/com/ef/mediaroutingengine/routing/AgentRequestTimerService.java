package com.ef.mediaroutingengine.routing;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
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
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
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
     * @param tasksPool        the tasks pool
     * @param jmsCommunicator  the jms communicator
     * @param tasksRepository  the tasks repository
     */
    @Autowired
    public AgentRequestTimerService(StepTimerService stepTimerService, TasksPool tasksPool,
                                    JmsCommunicator jmsCommunicator, TasksRepository tasksRepository) {
        this.stepTimerService = stepTimerService;
        this.tasksPool = tasksPool;
        this.jmsCommunicator = jmsCommunicator;
        this.tasksRepository = tasksRepository;
    }

    /**
     * Start.
     *
     * @param task  the task
     * @param queue the queue
     */
    public void start(Task task, PrecisionQueue queue) {
        this.stop(task.getTopicId());
        this.schedule(task.getTopicId(), getDelay(task.getChannelSession()), queue);
    }

    /**
     * Start on failover.
     *
     * @param task  the task
     * @param queue the queue
     */
    public void startOnFailover(Task task, PrecisionQueue queue) {
        this.stop(task.getTopicId());

        long delay = getDelay(task.getChannelSession()) - (System.currentTimeMillis() - task.getEnqueueTime());
        this.schedule(task.getTopicId(), delay, queue);
    }

    /**
     * Schedule.
     *
     * @param conversationId the conversation id
     * @param delay          the delay
     * @param queue          the queue
     */
    private void schedule(String conversationId, long delay, PrecisionQueue queue) {
        Timer timer = new Timer();
        this.timers.put(conversationId, timer);
        timer.schedule(new AgentRequestTimerService.RequestTimerTask(conversationId, queue), delay);
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
     * @param conversationId the conversation id
     */
    public void stop(String conversationId) {
        Timer timer = this.timers.get(conversationId);

        if (timer != null) {
            timer.cancel();
            this.timers.remove(conversationId);
        }
    }

    /**
     * The type Request timer task.
     */
    private class RequestTimerTask extends TimerTask {
        private final String conversationId;
        private final PrecisionQueue queue;

        /**
         * Instantiates a new Request ttl timer.
         *
         * @param conversation the topic id
         * @param queue        the queue
         */
        public RequestTimerTask(String conversation, PrecisionQueue queue) {
            this.conversationId = conversation;
            this.queue = queue;
        }

        public void run() {
            logger.info("Agent Request Ttl expired for request on conversation: {}", this.conversationId);

            synchronized (AgentRequestTimerService.this.tasksPool) {
                Task task = AgentRequestTimerService.this.tasksPool.findInProcessTaskFor(this.conversationId);

                if (task == null) {
                    logger.error("No In-Process Task found for this conversation, method returning...");
                    return;
                }

                synchronized (queue.getServiceQueue()) {
                    task.markForDeletion();
                }

                if (task.getTaskState().getName().name().equals("QUEUED")) {
                    logger.info("In process task: {} found in QUEUED state, removing task..", task.getId());

                    AgentRequestTimerService.this.stepTimerService.stop(task.getId());
                    AgentRequestTimerService.this.stop(conversationId);

                    queue.removeTask(task);

                    this.updateState(task.getTaskState());
                    this.remove(task);
                    this.publishEvents(task);

                    logger.info("Queued task: {} removed successfully", task.getId());
                } else if (task.getTaskState().getName().equals(Enums.TaskStateName.RESERVED)) {
                    logger.info("In process task: {} found in Reserved state, task is marked for deletion",
                            task.getId());
                }
            }
        }

        private void updateState(TaskState state) {
            state.setName(Enums.TaskStateName.CLOSED);
            state.setReasonCode(Enums.TaskStateReasonCode.NO_AGENT_AVAILABLE);
        }

        private void remove(Task task) {
            AgentRequestTimerService.this.tasksRepository.deleteById(task.getId());
            AgentRequestTimerService.this.tasksPool.remove(task);
        }

        private void publishEvents(Task task) {
            AgentRequestTimerService.this.jmsCommunicator.publishTaskStateChangeForReporting(task);
            AgentRequestTimerService.this.jmsCommunicator.publishNoAgentAvailable(task);
        }
    }
}
