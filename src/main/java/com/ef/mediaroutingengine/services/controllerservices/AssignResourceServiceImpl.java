package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.dto.TaskStateChangeRequest;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AssignResourceServiceImpl implements AssignResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignResourceServiceImpl.class);
    private final TasksPool tasksPool;
    private final Timer timer;
    private final JmsCommunicator jmsCommunicator;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param tasksPool pool of all tasks.
     * @param jmsCommunicator to publish event on a JMS topic.
     */
    @Autowired
    public AssignResourceServiceImpl(TasksPool tasksPool, JmsCommunicator jmsCommunicator) {
        this.tasksPool = tasksPool;
        this.timer = new Timer();
        this.jmsCommunicator = jmsCommunicator;
    }

    @Override
    public void assign(AssignResourceRequest request) {
        LOGGER.debug("assign method started");
        this.tasksPool.enqueueTask(request);
        LOGGER.debug("Task enqueued");
        //this.scheduleAgentRequestTimeoutTask(request.getChannelSession());
        LOGGER.debug("Agent Request Timeout Task scheduled");
        LOGGER.debug("assign method ended");
    }

    private void scheduleAgentRequestTimeoutTask(ChannelSession channelSession) {
        long delay = getDelay(channelSession);
        UUID topicId = channelSession.getTopicId();
        this.timer.schedule(new RequestTtlTimer(topicId), delay);
    }

    private long getDelay(ChannelSession channelSession) {
        int ttl = channelSession.getChannel().getChannelConfig().getRoutingPolicy().getAgentRequestTtl();
        return ttl * 1000L;
    }

    private class RequestTtlTimer extends TimerTask {
        private final UUID topicId;

        public RequestTtlTimer(UUID topicId) {
            this.topicId = topicId;
        }

        public void run() {
            Task task = AssignResourceServiceImpl.this.tasksPool.findByConversationId(topicId);
            if (task == null) {
                return;
            }

            Enums.TaskStateName stateName = task.getTaskState().getName();
            if (stateName.equals(Enums.TaskStateName.QUEUED)
                    || stateName.equals(Enums.TaskStateName.RESERVED)) {
                LOGGER.info("AgentRequestTtlTimer closed the task");
                TaskState newState = new TaskState(Enums.TaskStateName.CLOSED,
                        Enums.TaskStateReasonCode.NO_AGENT_AVAILABLE);
                TaskStateChangeRequest data = new TaskStateChangeRequest(task.getId(), newState);
                try {
                    AssignResourceServiceImpl.this.jmsCommunicator
                            .publish(data, Enums.RedisEventName.TASK_STATE_CHANGED);
                    // publish no-agent-available.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
