package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.TaskRouter;
import com.ef.mediaroutingengine.routing.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrecisionQueuesServiceImplTest {

    @Mock
    private PrecisionQueueRepository repository;

    @Mock
    private PrecisionQueuesPool precisionQueuesPool;

    @Mock
    private TasksPool tasksPool;

    @Mock
    private MrdPool mrdPool;

    @Mock
    private TaskManager taskManager;

    private PrecisionQueuesServiceImpl precisionQueuesService;

    @BeforeEach
    void setUp() {
        this.precisionQueuesService = new PrecisionQueuesServiceImpl(repository, precisionQueuesPool, mrdPool, tasksPool
                , taskManager);
    }

    @Test
    void test_onCreate() {
        PrecisionQueueRequestBody requestBody = getPrecisionQueueRequest();
        PrecisionQueueEntity precisionQueueEntity = mock(PrecisionQueueEntity.class);

        PrecisionQueuesServiceImpl spy = Mockito.spy(precisionQueuesService);

        doNothing().when(spy).throwExceptionIfQueueNameIsNotUnique(requestBody, null);
        doNothing().when(spy).validateAndSetMrd(requestBody);
        doReturn(precisionQueueEntity).when(this.repository).insert((PrecisionQueueEntity) any());
        doReturn(mock(TaskRouter.class)).when(spy).getTaskSchedulerBean();

        spy.create(requestBody);

        ArgumentCaptor<PrecisionQueue> precisionQueueRequestBodyArgumentCaptor = ArgumentCaptor.forClass(PrecisionQueue.class);
        verify(repository).insert((PrecisionQueueEntity) any());
        verify(precisionQueuesPool).insert(precisionQueueRequestBodyArgumentCaptor.capture());

    }

    @Test
    void testNotFoundException_whenQueueNotExistInRepository() {
        PrecisionQueueRequestBody requestBody = getPrecisionQueueRequest();
        PrecisionQueueEntity entity = new PrecisionQueueEntity();
        entity.setId(requestBody.getId());
        entity.setName(requestBody.getName());
        entity.setMrd(requestBody.getMrd());
        entity.setServiceLevelType(requestBody.getServiceLevelType());
        entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());
        String queueId = entity.getId();

        assertThrows(NotFoundException.class, () -> precisionQueuesService.retrieve(queueId));
    }

    @Test
    void test_successfulRetrieve() {
        String queueId = UUID.randomUUID().toString();
        PrecisionQueueRequestBody requestBody = getPrecisionQueueRequest();
        PrecisionQueueEntity entity = new PrecisionQueueEntity();
        entity.setId(queueId);
        entity.setName(requestBody.getName());
        entity.setMrd(requestBody.getMrd());
        entity.setServiceLevelType(requestBody.getServiceLevelType());
        entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());

        doReturn(false).when(this.repository).existsById(queueId);
        doReturn(true).when(this.repository).existsById(queueId);
        doReturn(Optional.of(entity)).when(this.repository).findById(queueId);

        PrecisionQueuesServiceImpl spy = Mockito.spy(precisionQueuesService);

        ResponseEntity<Object> result = spy.retrieve(queueId);
        assertEquals(HttpStatus.OK, result.getStatusCode());

    }

    @Test
    void test_onUpdate() {
        String queueId = UUID.randomUUID().toString();
        PrecisionQueueRequestBody requestBody = getPrecisionQueueRequest();

        PrecisionQueueEntity entity = new PrecisionQueueEntity();
        entity.setId(queueId);
        entity.setName(requestBody.getName());
        entity.setMrd(requestBody.getMrd());
        entity.setServiceLevelType(requestBody.getServiceLevelType());
        entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());

        Optional<PrecisionQueueEntity> existingEntity = Optional.of(entity);
        PrecisionQueue precisionQueue = mock(PrecisionQueue.class);

        PrecisionQueuesServiceImpl spy = Mockito.spy(precisionQueuesService);

        doNothing().when(spy).throwExceptionIfQueueNameIsNotUnique(requestBody, queueId);
        doNothing().when(spy).validateAndSetMrd(requestBody);
        doReturn(existingEntity).when(this.repository).findById(queueId);
        doReturn(precisionQueue).when(this.precisionQueuesPool).findById(queueId);

        PrecisionQueueEntity result = spy.update(requestBody, queueId);

        verify(precisionQueue, times(1)).updateQueue(requestBody);
        verify(this.repository, times(1)).save(existingEntity.get());
        assertEquals(queueId, result.getId());
    }

    @Test
    void test_onDelete() {
        String queueId = UUID.randomUUID().toString();
        doReturn(true).when(this.repository).existsById(queueId);
        doReturn(new ArrayList<>()).when(this.tasksPool).findByQueueId(queueId);
        doReturn(mock(PrecisionQueue.class)).when(this.precisionQueuesPool).findById(queueId);

        ResponseEntity<Object> response = precisionQueuesService.delete(queueId);

        verify(precisionQueuesPool).deleteById(queueId);
        verify(repository).deleteById(queueId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    private PrecisionQueueRequestBody getPrecisionQueueRequest() {
        PrecisionQueueRequestBody precisionQueueRequestBody = new PrecisionQueueRequestBody();
        precisionQueueRequestBody.setId(UUID.randomUUID().toString());
        precisionQueueRequestBody.setName("test");
        precisionQueueRequestBody.setMrd(getMRD());
        precisionQueueRequestBody.setServiceLevelThreshold(3);
        return precisionQueueRequestBody;
    }

    private MediaRoutingDomain getMRD() {
        MediaRoutingDomain mediaRoutingDomain = new MediaRoutingDomain();
        mediaRoutingDomain.setId(UUID.randomUUID().toString());
        mediaRoutingDomain.setName("chat");
        mediaRoutingDomain.setInterruptible(false);
        return mediaRoutingDomain;
    }
}

