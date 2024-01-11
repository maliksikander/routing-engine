package com.ef.mediaroutingengine.routing.queue;

import com.ef.mediaroutingengine.routing.model.QueueTask;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.validation.constraints.NotNull;

/**
 * The type Priority queue.
 */
public class PriorityQueue {
    /**
     * The Multi level queue map.
     */
    private final Map<Integer, ConcurrentLinkedQueue<QueueTask>> multiLevelQueueMap;
    /**
     * The No of queue levels.
     */
    private static final int NO_OF_QUEUE_LEVELS = 11;

    /**
     * Instantiates a new Priority queue.
     */
    public PriorityQueue() {
        this.multiLevelQueueMap = new ConcurrentHashMap<>();
        for (int i = NO_OF_QUEUE_LEVELS; i >= 1; i--) {
            this.multiLevelQueueMap.put(i, new ConcurrentLinkedQueue<>());
        }
    }

    /**
     * Enqueue boolean.
     *
     * @param task the task service
     * @return the boolean
     */
    public boolean enqueue(@NotNull QueueTask task) {
        return this.multiLevelQueueMap.get(task.getPriority()).offer(task);
    }

    /**
     * Dequeue task service.
     *
     * @param poll Task will be removed from queue if poll is true (queue poll operation), task will not be
     *             removed from queue otherwise (queue peek operation)
     * @return the task service in both cases (poll or peak), returns null if task not found
     */
    public QueueTask dequeue(boolean poll) {
        for (int i = NO_OF_QUEUE_LEVELS; i >= 1; i--) {
            if (!this.multiLevelQueueMap.get(i).isEmpty()) {
                return poll
                        ? this.multiLevelQueueMap.get(i).poll()
                        : this.multiLevelQueueMap.get(i).peek();
            }
        }
        return null;
    }

    /**
     * Remove by task id boolean.
     *
     * @param taskId the task id
     */
    public void remove(@NotNull String taskId) {
        for (Map.Entry<Integer, ConcurrentLinkedQueue<QueueTask>> entry : this.multiLevelQueueMap.entrySet()) {
            entry.getValue().removeIf(t -> t.getTaskId().equals(taskId));
        }
    }

    /**
     * Size int.
     *
     * @return the int size of the Priority queue
     */
    public int size() {
        int size = 0;
        for (int i = NO_OF_QUEUE_LEVELS; i >= 1; i--) {
            size = size + this.multiLevelQueueMap.get(i).size();
        }
        return size;
    }

    /**
     * Task exists boolean.
     *
     * @param taskId the task id
     * @return the boolean
     */
    public boolean exists(String taskId) {
        for (Map.Entry<Integer, ConcurrentLinkedQueue<QueueTask>> entry : this.multiLevelQueueMap.entrySet()) {
            for (QueueTask task1 : entry.getValue()) {
                if (taskId.equals(task1.getId())) {
                    return true;
                }
            }
        }
        return false;
    }



    /**
     * Returns the list of enqueued tasks.
     *
     * @return list of enqueued tasks
     */
    public List<QueueTask> getAll() {
        List<QueueTask> taskList = new LinkedList<>();
        for (Map.Entry<Integer, ConcurrentLinkedQueue<QueueTask>> entry : this.multiLevelQueueMap.entrySet()) {
            taskList.addAll(entry.getValue());
        }
        return taskList;
    }

    /**
     * Gets position.
     *
     * @param taskId   the task id
     * @param priority the priority
     * @return the position
     */
    public int getPosition(String taskId, int priority) {
        int positionInSamePriority = getPositionInSamePriority(taskId, priority);

        if (positionInSamePriority == -1) {
            return -1;
        }

        int position = positionInSamePriority;

        for (int i = priority + 1; i <= NO_OF_QUEUE_LEVELS; i++) {
            position += this.multiLevelQueueMap.get(i).size();
        }

        return position;
    }

    private int getPositionInSamePriority(String taskId, int priority) {
        int position = 1;
        for (QueueTask queuedTask : this.multiLevelQueueMap.get(priority)) {
            if (queuedTask.getTaskId().equals(taskId)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("->/");

        for (int i = NO_OF_QUEUE_LEVELS; i >= 1; i--) {
            for (QueueTask task : this.multiLevelQueueMap.get(i)) {
                result.append("Task ID: ")
                        .append(task.getTaskId())
                        .append(", Media ID: ")
                        .append(task.getMediaId())
                        .append("|");
            }
        }

        String strResult = result.toString();
        String end = strResult.substring(strResult.length() - 1).equalsIgnoreCase("/")
                ? strResult + "/<-" : strResult.substring(0, strResult.length() - 1) + "/<-";

        result.append(end);

        return result.toString();
    }
}
