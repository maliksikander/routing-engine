package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.PrecisionQueueEntityRepository;
import com.ef.mediaroutingengine.services.TaskScheduler;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    /**
     * The Repository.
     */
    private final PrecisionQueueEntityRepository repository;

    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;

    private final MrdPool mrdPool;

    /**
     * Default constructor.
     *
     * @param repository precision queue repository
     */
    @Autowired
    public PrecisionQueuesServiceImpl(PrecisionQueueEntityRepository repository,
                                      PrecisionQueuesPool precisionQueuesPool,
                                      MrdPool mrdPool) {
        this.repository = repository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.mrdPool = mrdPool;
    }

    @Override
    public PrecisionQueueEntity create(PrecisionQueueRequestBody requestBody) {
        requestBody.setId(UUID.randomUUID());
        this.validateAndSetMRD(requestBody);
        this.precisionQueuesPool.insert(new PrecisionQueue(requestBody, getTaskSchedulerBean()));
        return repository.insert(new PrecisionQueueEntity(requestBody));
    }

    /**
     * Gets task scheduler bean.
     *
     * @return the task scheduler bean
     */
    @Lookup
    public TaskScheduler getTaskSchedulerBean() {
        return null;
    }

    @Override
    public List<PrecisionQueueEntity> retrieve() {
        return this.repository.findAll();
    }

    @Override
    public PrecisionQueueEntity update(PrecisionQueueRequestBody requestBody, UUID id) {
        requestBody.setId(id);
        this.validateAndSetMRD(requestBody);

        Optional<PrecisionQueueEntity> existing = this.repository.findById(id);
        if (!existing.isPresent()) {
            throw new NotFoundException("Could not find precision queue resource to update");
        }
        PrecisionQueueEntity precisionQueueEntity = existing.get();
        precisionQueueEntity.updateQueue(requestBody);
        this.precisionQueuesPool.findById(id).updateQueue(requestBody);
        return this.repository.save(precisionQueueEntity);
    }

    @Override
    public ResponseEntity<Object> delete(UUID id) {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find precision resource to delete");
        }
        List<Task> tasks = this.precisionQueuesPool.findById(id).getTasks();
        if (tasks.isEmpty()) {
            this.precisionQueuesPool.deleteById(id);
            this.repository.deleteById(id);
            return new ResponseEntity<>(new SuccessResponseBody("Successfully deleted"), HttpStatus.OK);
        }
        return new ResponseEntity<>(tasks, HttpStatus.CONFLICT);
    }

    private void validateAndSetMRD(PrecisionQueueRequestBody requestBody) {
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
