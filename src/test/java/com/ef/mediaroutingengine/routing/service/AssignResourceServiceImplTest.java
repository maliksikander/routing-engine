package com.ef.mediaroutingengine.routing.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MrdType;
import com.ef.cim.objectmodel.dto.AssignResourceRequest;
import com.ef.cim.objectmodel.enums.MrdTypeName;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.cim.objectmodel.task.TaskQueue;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.cim.objectmodel.task.TaskType;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssignResourceServiceImplTest {
    @Mock
    private TaskManager taskManager;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private MrdPool mrdPool;

    private AssignResourceServiceImpl assignResourceService;

    @BeforeEach
    void setUp() {
        assignResourceService = new AssignResourceServiceImpl(taskManager, tasksRepository, mrdPool);
    }

    @Nested
    @DisplayName("Assign resource for Inbound Request tests")
    class AssignInboundTests {
        @Test
        void test_aRequestIsReceived_when_noTasksExistForThisConversation() {
            String mrdId = new ObjectId().toString();
            String conversationId = UUID.randomUUID().toString();

            AssignResourceRequest request = createInboundRequest(conversationId, mrdId);
            PrecisionQueue queue = mock(PrecisionQueue.class);

            when(tasksRepository.findAllByConversationId(conversationId)).thenReturn(new ArrayList<>());
            assignResourceService.assign(conversationId, request, queue);

            verifyNoInteractions(mrdPool);
            verify(taskManager, times(1)).enqueueTask(request, mrdId, queue);
            verifyNoMoreInteractions(taskManager);
        }

        @Test
        void ignoreReq_when_anAutoJoinAbleReqIsReceived_and_anInboundAutoJoinAbleQueuedTaskExistsOnConversation() {
            String mrdId = new ObjectId().toString();
            String conversationId = UUID.randomUUID().toString();

            AssignResourceRequest request = createInboundRequest(conversationId, mrdId);
            PrecisionQueue queue = mock(PrecisionQueue.class);

            String taskId = UUID.randomUUID().toString();
            TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, new HashMap<>());
            TaskMedia media = createMedia(mrdId, taskId, conversationId, TaskMediaState.QUEUED, type);

            List<Task> existingTasks = new ArrayList<>();
            existingTasks.add(createTask(media));

            when(tasksRepository.findAllByConversationId(conversationId)).thenReturn(existingTasks);
            when(mrdPool.getType(mrdId)).thenReturn(new MrdType("", MrdTypeName.CHAT, true, true, true));

            assignResourceService.assign(conversationId, request, queue);

            verifyNoMoreInteractions(mrdPool);
            verifyNoInteractions(taskManager);
        }

        @Test
        void ignoreReq_when_anAutoJoinAbleReqIsReceived_and_anInboundAutoJoinAbleReservedTaskExistsOnConversation() {
            String mrdId = new ObjectId().toString();
            String conversationId = UUID.randomUUID().toString();

            AssignResourceRequest request = createInboundRequest(conversationId, mrdId);
            PrecisionQueue queue = mock(PrecisionQueue.class);

            String taskId = UUID.randomUUID().toString();
            TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, new HashMap<>());
            TaskMedia media = createMedia(mrdId, taskId, conversationId, TaskMediaState.RESERVED, type);

            List<Task> existingTasks = new ArrayList<>();
            existingTasks.add(createTask(media));

            when(tasksRepository.findAllByConversationId(conversationId)).thenReturn(existingTasks);
            when(mrdPool.getType(mrdId)).thenReturn(new MrdType("", MrdTypeName.CHAT, true, true, true));

            assignResourceService.assign(conversationId, request, queue);

            verifyNoMoreInteractions(mrdPool);
            verifyNoInteractions(taskManager);
        }

    }

    AssignResourceRequest createInboundRequest(String conversationId, String mrdId) {
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, new HashMap<>());
        return createRequest(type, conversationId, mrdId);
    }

    AssignResourceRequest createRequest(TaskType type, String conversationId, String mrdId) {
        ChannelSession channelSession = createChannelSession(conversationId, mrdId);
        List<ChannelSession> channelSessions = new ArrayList<>();
        channelSessions.add(channelSession);

        AssignResourceRequest request = new AssignResourceRequest();
        request.setRequestSession(channelSession);
        request.setChannelSessions(channelSessions);
        request.setType(type);

        return request;
    }

    ChannelSession createChannelSession(String conversationId, String mrdId) {
        ChannelSession channelSession = new ChannelSession();
        channelSession.setConversationId(conversationId);
        channelSession.getChannel().getChannelType().setMediaRoutingDomain(mrdId);
        return channelSession;
    }

    TaskMedia createMedia(String mrdId, String taskId, String conversationId, TaskMediaState state, TaskType type) {
        ChannelSession channelSession = createChannelSession(conversationId, mrdId);

        List<ChannelSession> channelSessions = new ArrayList<>();
        channelSessions.add(channelSession);

        return new TaskMedia(mrdId, taskId, new TaskQueue(), type, 1, state, channelSession, channelSessions);
    }

    Task createTask(TaskMedia... medias) {
        String taskId = medias[0].getTaskId();
        String conversationId = medias[0].getRequestSession().getConversationId();
        TaskState state = new TaskState(Enums.TaskStateName.ACTIVE, null);

        List<TaskMedia> activeMedia = List.of(medias);
        return new Task(taskId, conversationId, state, null, UUID.randomUUID().toString(), activeMedia);
    }
}