package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.AssignResourceRequest;
import com.ef.cim.objectmodel.task.Task;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.List;
import java.util.ListIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Assign resource service.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AssignResourceServiceImpl implements AssignResourceService {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(AssignResourceServiceImpl.class);

    /**
     * The Task manager.
     */
    private final TaskManager taskManager;
    private final TasksRepository tasksRepository;
    private final MrdPool mrdPool;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param taskManager     the task manager
     * @param tasksRepository the tasks repository
     * @param mrdPool         the mrd pool
     */
    @Autowired
    public AssignResourceServiceImpl(TaskManager taskManager, TasksRepository tasksRepository, MrdPool mrdPool) {
        this.taskManager = taskManager;
        this.tasksRepository = tasksRepository;
        this.mrdPool = mrdPool;
    }

    @Override
    public void assign(String conversationId, AssignResourceRequest request, PrecisionQueue queue) {
        List<Task> tasks = this.tasksRepository.findAllByConversation(conversationId);
        String mrdId = request.getRequestSession().getChannel().getChannelType().getMediaRoutingDomain();

        logger.info("{} tasks exist on conversation: {}", tasks.size(), conversationId);

        if (request.getType().getDirection().equals(Enums.TaskTypeDirection.DIRECT_TRANSFER)
                || request.getType().getDirection().equals(Enums.TaskTypeDirection.DIRECT_CONFERENCE)) {
            handleTransferConference(request, queue, tasks, mrdId);
        } else {
            this.handleInboundOutbound(request, queue, tasks, mrdId);
        }
    }

    private void handleInboundOutbound(AssignResourceRequest req, PrecisionQueue queue, List<Task> tasks,
                                       String mrdId) {
        if (req.getLastAssignedAgentId() != null) {
            req.getType().putMetadata("lastAssignedAgentId", req.getLastAssignedAgentId());
        }

        if (!tasks.isEmpty()) {
            if (this.mrdPool.getType(mrdId).isAutoJoin()) {
                logger.info("Request is autoJoinAble, returning...");
                return;
            }

            String customerIdentifier = req.getRequestSession().getChannelData().getChannelCustomerIdentifier();

            if (this.taskExistByCustomerIdentifier(tasks, customerIdentifier)) {
                logger.info("Tasks exist for customer identifier: {}, returning...", customerIdentifier);
                return;
            }

            ListIterator<Task> itr = tasks.listIterator();
            while (itr.hasNext()) {
                Task task = itr.next();
                boolean isRevoked = this.taskManager.revokeInProcessTask(task, true);
                if (isRevoked) {
                    itr.remove();
                }
            }

            boolean isAssigned = this.taskManager.reserveCurrentAvailable(req, tasks, mrdId, queue.toTaskQueue());

            if (isAssigned) {
                return;
            }
        }

        this.taskManager.enqueueTask(req, mrdId, queue);

    }

    private void handleTransferConference(AssignResourceRequest req, PrecisionQueue queue, List<Task> tasks,
                                          String mrdId) {
        if (!tasks.isEmpty()) {
            if (this.mrdPool.getType(mrdId).isAutoJoin() && anyInProcessTaskExists(tasks)) {
                String error = "A previous request is in process of finding an agent on this conversation"
                        + ", cannot process this request.";
                logger.error(error);
                throw new IllegalStateException(error);
            } else if (!this.mrdPool.getType(mrdId).isAutoJoin()) {
                tasks.forEach(t -> this.taskManager.revokeInProcessTask(t, true));
            }
        }

        this.taskManager.enqueueTask(req, mrdId, queue);
    }

    private boolean anyInProcessTaskExists(List<Task> tasks) {
        return tasks.stream().anyMatch(t -> t.findInProcessMedia() != null);
    }

    private boolean taskExistByCustomerIdentifier(List<Task> tasks, String customerIdentifier) {
        return tasks.stream().anyMatch(t -> t.getActiveMedia().stream()
                .anyMatch(m -> m.getRequestSession().getChannelData().getChannelCustomerIdentifier()
                        .equals(customerIdentifier)));
    }
}