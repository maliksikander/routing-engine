package com.ef.mediaroutingengine.routing.queue;

import static org.assertj.core.api.Assertions.assertThat;


import com.ef.mediaroutingengine.taskmanager.model.Task;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PriorityQueueTest {

    private static final int NO_OF_QUEUE_LEVELS = 45;
    @Mock
    private Map<Integer, ConcurrentLinkedQueue<Task>> multiLevelQueueMap;
    @InjectMocks
    private PriorityQueue underTest;

    @Nested
    class WhenEnqueuing {
        @Mock
        private @NotNull Task task;

        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenDequeuing {
        private final boolean POLL = true;

        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenSizing {
        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenGetting {
        private final int INDEX = 37;

        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenIndexingOf {
        private final String TASK_ID = "TASK_ID";

        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenGettingTask {
        private final String TASK_ID = "TASK_ID";

        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenTaskingExists {
        @Mock
        private @NotNull Task task;

        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenTaskingExists2 {
        private final UUID TASK_ID = UUID.randomUUID();

        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenRemoving {
        @Mock
        private @NotNull Task task;

        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenGettingEnqueuedTasksList {
        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenRestoringBackup {
        @Mock
        private Task task;

        @BeforeEach
        void setup() {
        }
    }

    @Nested
    class WhenGettingMaxTime {
        @BeforeEach
        void setup() {
        }
    }
}