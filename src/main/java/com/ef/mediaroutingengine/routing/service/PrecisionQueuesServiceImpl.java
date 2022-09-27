package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.TaskRouter;
import com.ef.mediaroutingengine.routing.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * The type Precision queue entity service.
 */
@Service
public class PrecisionQueuesServiceImpl implements PrecisionQueuesService {
    private static final Logger logger = LoggerFactory.getLogger(PrecisionQueuesServiceImpl.class);
    /**
     * The Repository.
     */
    private final PrecisionQueueRepository repository;

    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;

    /**
     * The Mrd pool.
     */
    private final MrdPool mrdPool;
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;

    /**
     * Default constructor.
     *
     * @param repository          precision queue repository
     * @param precisionQueuesPool the precision queues pool
     * @param mrdPool             the mrd pool
     */
    @Autowired
    public PrecisionQueuesServiceImpl(PrecisionQueueRepository repository,
                                      PrecisionQueuesPool precisionQueuesPool,
                                      MrdPool mrdPool, TasksPool tasksPool, TaskManager taskManager) {
        this.repository = repository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.mrdPool = mrdPool;
        this.tasksPool = tasksPool;
        this.taskManager = taskManager;
    }

    @Override
    public PrecisionQueueEntity create(PrecisionQueueRequestBody requestBody) {
        logger.info("Create PrecisionQueue request initiated");

        this.throwExceptionIfQueueNameIsNotUnique(requestBody, null);

        this.validateAndSetMrd(requestBody);
        logger.debug("MRD validated for PrecisionQueue");

        PrecisionQueueEntity inserted = repository.insert(AdapterUtility.createQueueEntityFrom(requestBody));
        logger.debug("PrecisionQueue inserted in PrecisionQueue Config DB | Queue: {}", inserted.getId());

        this.precisionQueuesPool.insert(new PrecisionQueue(inserted, getTaskSchedulerBean()));
        logger.debug("PrecisionQueue inserted in in-memory PrecisionQueue pool | Queue: {}", inserted.getId());

        logger.info("PrecisionQueue created successfully | Queue: {}", inserted.getId());
        return inserted;
    }

    /**
     * Gets task scheduler bean.
     *
     * @return the task scheduler bean
     */
    @Lookup
    public TaskRouter getTaskSchedulerBean() {
        return null;
    }

    @Override
    public ResponseEntity<Object> retrieve(String queueId) {
        if (queueId != null && !repository.existsById(queueId)) {
            String errorMessage = "Could not find the PrecisionQueue resource with id: " + queueId;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        if (queueId != null && repository.existsById(queueId)) {
            PrecisionQueueEntity precisionQueue = this.repository.findById(queueId).get();
            logger.debug("PrecisionQueue existed in DB. | PrecisionQueue:  {}", precisionQueue);
            return new ResponseEntity<>(precisionQueue, HttpStatus.OK);
        }
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @Override
    public PrecisionQueueEntity update(PrecisionQueueRequestBody requestBody, String id) {
        logger.info("Update PrecisionQueue request initiated | Queue: {}", id);

        this.throwExceptionIfQueueNameIsNotUnique(requestBody, id);

        requestBody.setId(id);
        this.validateAndSetMrd(requestBody);
        logger.debug("MRD validated for PrecisionQueue | Queue: {}", id);

        Optional<PrecisionQueueEntity> existing = this.repository.findById(id);
        if (existing.isEmpty()) {
            String errorMessage = "Could not find PrecisionQueue resource to update | QueueId: " + id;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        PrecisionQueueEntity precisionQueueEntity = existing.get();
        AdapterUtility.updateQueueEntityFrom(requestBody, precisionQueueEntity);

        this.precisionQueuesPool.findById(id).updateQueue(requestBody);
        logger.debug("PrecisionQueue updated in in-memory PrecisionQueue pool | Queue: {}", id);

        this.repository.save(precisionQueueEntity);
        logger.debug("PrecisionQueue updated in PrecisionQueue Config DB | Queue: {}", id);

        logger.info("PrecisionQueue updated successfully | Queue: {}", id);
        return precisionQueueEntity;
    }

    @Override
    public ResponseEntity<Object> delete(String id) {
        logger.info("Delete PrecisionQueue request initiated | Queue: {}", id);

        if (!this.repository.existsById(id)) {
            String errorMessage = "Could not find the PrecisionQueue resource to delete | Queue: " + id;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        List<Task> tasks = this.tasksPool.findByQueueId(id);
        if (tasks.isEmpty()) {
            PropertyChangeListener listener = this.precisionQueuesPool.findById(id).getTaskScheduler();
            this.taskManager.removePropertyChangeListener(Enums.EventName.NEW_TASK.name(), listener);
            logger.debug("PrecisionQueue's TaskRouter removed from the TaskManager's PropertyChangeListener list");

            this.precisionQueuesPool.deleteById(id);
            logger.debug("PrecisionQueue deleted from the in-memory PrecisionQueue pool | Queue: {}", id);

            this.repository.deleteById(id);
            logger.debug("PrecisionQueue deleted from the PrecisionQueue Config DB | Queue: {}", id);

            logger.info("PrecisionQueue Deleted successfully | Queue: {}", id);
            return new ResponseEntity<>(new SuccessResponseBody("Successfully deleted"), HttpStatus.OK);
        }

        logger.info("Could not delete PrecisionQueue, there are tasks associated to it | Queue: {}", id);
        List<TaskDto> taskDtoList = new ArrayList<>();
        tasks.forEach(task -> taskDtoList.add(AdapterUtility.createTaskDtoFrom(task)));
        return new ResponseEntity<>(taskDtoList, HttpStatus.CONFLICT);
    }

    void throwExceptionIfQueueNameIsNotUnique(PrecisionQueueRequestBody requestBody, String queueId) {
        for (PrecisionQueue queue : this.precisionQueuesPool.toList()) {
            if (!queue.getId().equals(queueId) && queue.getName().equals(requestBody.getName())) {
                String errorMessage = "A queue already exist with name: " + requestBody.getName();
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    /**
     * Validate and set mrd.
     *
     * @param requestBody the request body
     */
    void validateAndSetMrd(PrecisionQueueRequestBody requestBody) {
        if (requestBody.getMrd().getId() == null) {
            throw new IllegalArgumentException("MRD-id is null");
        }
        MediaRoutingDomain mediaRoutingDomain = this.mrdPool.findById(requestBody.getMrd().getId());
        if (mediaRoutingDomain == null) {
            throw new NotFoundException("Could not find media-routing-domain resource");
        }
        requestBody.setMrd(mediaRoutingDomain);
    }
}
