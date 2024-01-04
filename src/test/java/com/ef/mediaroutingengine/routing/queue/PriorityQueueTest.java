package com.ef.mediaroutingengine.routing.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskQueue;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PriorityQueueTest {
    private PriorityQueue priorityQueue;

    @BeforeEach
    void setUp() {
        this.priorityQueue = new PriorityQueue();
    }

    @Test
    void testGetPosition_when_thereTaskIsNotInQueue() {
        Task task = this.createTask(1);
        assertEquals(-1, this.priorityQueue.getPosition(task));
    }

    @Test
    void testGetPosition_when_thereIsSingleTask() {
        Task task = this.createTask(1);
        this.priorityQueue.enqueue(task);
        assertEquals(1, this.priorityQueue.getPosition(task));

        this.priorityQueue.dequeue(true);

        task.setPriority(11);
        this.priorityQueue.enqueue(task);
        assertEquals(1, this.priorityQueue.getPosition(task));

        this.priorityQueue.dequeue(true);

        task.setPriority(6);
        this.priorityQueue.enqueue(task);
        assertEquals(1, this.priorityQueue.getPosition(task));
    }

    @Test
    void testGetPosition_when_thereAreMultipleTasks() {
        Task task1 = this.createTask(1);
        Task task2 = this.createTask(11);
        Task task3 = this.createTask(11);
        Task task4 = this.createTask(6);
        Task task5 = this.createTask(6);

        Task task6 = this.createTask(1);

        this.priorityQueue.enqueue(task1);
        this.priorityQueue.enqueue(task2);
        this.priorityQueue.enqueue(task3);
        this.priorityQueue.enqueue(task4);

        assertEquals(4, this.priorityQueue.getPosition(task1));
        assertEquals(1, this.priorityQueue.getPosition(task2));
        assertEquals(2, this.priorityQueue.getPosition(task3));
        assertEquals(3, this.priorityQueue.getPosition(task4));

        this.priorityQueue.enqueue(task5);

        assertEquals(5, this.priorityQueue.getPosition(task1));
        assertEquals(1, this.priorityQueue.getPosition(task2));
        assertEquals(2, this.priorityQueue.getPosition(task3));
        assertEquals(3, this.priorityQueue.getPosition(task4));
        assertEquals(4, this.priorityQueue.getPosition(task5));

        assertEquals(-1, this.priorityQueue.getPosition(task6));

        this.priorityQueue.dequeue(true);
        assertEquals(1, this.priorityQueue.getPosition(task3));
    }

    private ChannelSession getNewChannelSession() {
        ChannelSession channelSession = new ChannelSession();
        channelSession.setId(UUID.randomUUID().toString());
        channelSession.setConversationId(UUID.randomUUID().toString());
        return channelSession;
    }

    private MediaRoutingDomain getMrd() {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName("Chat");
        mrd.setDescription("Description");
        return mrd;
    }

    private Task createTask(int priority) {
        TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, null);
        TaskQueue taskQueue = new TaskQueue(UUID.randomUUID().toString(), "Chat");
        return Task.getInstanceFrom(getNewChannelSession(), getMrd(), taskQueue, taskState, type, priority);
    }
}