package com.ef.mediaroutingengine.global.locks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

/**
 * The type Conversation locks pool.
 */
@Service
public class ConversationLock {
    private static class LockWrapper {
        private final Lock lock = new ReentrantLock();
        private final AtomicInteger numberOfThreadsInQueue = new AtomicInteger(1);

        LockWrapper addThread() {
            numberOfThreadsInQueue.incrementAndGet();
            return this;
        }

        int removeThread() {
            return numberOfThreadsInQueue.decrementAndGet();
        }

        void lock() {
            lock.lock();
        }
    }

    /**
     * The constant locks.
     */
    private static final Map<String, LockWrapper> locks = new ConcurrentHashMap<>();

    /**
     * Lock.
     *
     * @param conversationId the conversation id
     */
    public void lock(String conversationId) {
        LockWrapper lock = locks.compute(conversationId, (k, v) -> v == null ? new LockWrapper() : v.addThread());
        lock.lock();
    }

    /**
     * Unlock.
     *
     * @param conversationId the conversation id
     */
    public void unlock(String conversationId) {
        LockWrapper lockWrapper = locks.get(conversationId);
        lockWrapper.lock.unlock();
        if (lockWrapper.removeThread() == 0) {
            // NB : We pass in the specific value to remove to handle the case where another thread would queue
            // right before the removal
            locks.remove(conversationId, lockWrapper);
        }
    }
}
