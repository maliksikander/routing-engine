package com.ef.mediaroutingengine.services.pools;

import com.ef.mediaroutingengine.model.PrecisionQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrecisionQueuesPoolTest {
    private PrecisionQueuesPool pool;

    @BeforeEach
    void setUp() {
        pool = new PrecisionQueuesPool();
    }

    @Test
    void test_findName_returnsQueueObject_when_queueIsFound() {
        PrecisionQueue queue = mock(PrecisionQueue.class);
        String queueId = "1";
        String name = "chat-queue";

        when(queue.getId()).thenReturn(queueId);
        this.pool.insert(queue);

        when(queue.getName()).thenReturn(name);
        assertEquals(queue, this.pool.findByName(name));
    }

    @Test
    void test_findName_returnsNull_when_queueIsNotFound() {
        PrecisionQueue queue = mock(PrecisionQueue.class);
        String queueId = "1";
        String name = "chat-queue";

        when(queue.getId()).thenReturn(queueId);
        this.pool.insert(queue);

        when(queue.getName()).thenReturn(name);
        assertNull(this.pool.findByName("NotExistingQueueName"));
    }
}