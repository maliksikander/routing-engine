package com.ef.mediaroutingengine.routing.queue;

import com.ef.mediaroutingengine.taskmanager.model.Task;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
    private final Map<Integer, ConcurrentLinkedQueue<Task>> multiLevelQueueMap;
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
    public boolean enqueue(@NotNull Task task) {
        return this.multiLevelQueueMap.get(task.getPriority()).offer(task);
    }

    /**
     * Dequeue task service.
     *
     * @param poll Task will be removed from queue if poll is true (queue poll operation), task will not be
     *             removed from queue otherwise (queue peek operation)
     * @return the task service in both cases (poll or peak), returns null if task not found
     */
    public Task dequeue(boolean poll) {
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
     * Index of int.
     *
     * @param taskId the task id
     * @return the int
     */
    public int indexOf(String taskId) {
        int index = -1;
        int previousQueuesSize = 0;
        for (int i = NO_OF_QUEUE_LEVELS; i >= 1; i--) {
            Queue<Task> queue = this.multiLevelQueueMap.get(i);
            int k = 0;
            for (Task task : queue) {
                if (task.getId().equals(taskId)) {
                    index = previousQueuesSize + k;
                    return index;
                }
                k++;
            }
            previousQueuesSize = previousQueuesSize + queue.size();
        }
        return index;
    }

    /**
     * Get task service.
     *
     * @param taskId the task id
     * @return the task service
     */
    public Task getTask(String taskId) {
        for (Map.Entry<Integer, ConcurrentLinkedQueue<Task>> entry : this.multiLevelQueueMap.entrySet()) {
            for (Task task : entry.getValue()) {
                if (task.getId().equals(taskId)) {
                    return task;
                }
            }
        }
        return null;
    }

    /**
     * Task exists boolean.
     *
     * @param task the task service
     * @return the boolean
     */
    public boolean taskExists(@NotNull Task task) {
        return taskExists(task.getId());
    }

    /**
     * Task exists boolean.
     *
     * @param taskId the task id
     * @return the boolean
     */
    public boolean taskExists(String taskId) {
        for (Map.Entry<Integer, ConcurrentLinkedQueue<Task>> entry : this.multiLevelQueueMap.entrySet()) {
            for (Task task1 : entry.getValue()) {
                if (taskId.equals(task1.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove boolean.
     *
     * @param task the task service
     * @return the boolean
     */
    public boolean remove(@NotNull Task task) {
        for (Map.Entry<Integer, ConcurrentLinkedQueue<Task>> entry : this.multiLevelQueueMap.entrySet()) {
            for (Task t : entry.getValue()) {
                if (t.getId().equals(task.getId())) {
                    entry.getValue().remove(t);
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
    public List<Task> getEnqueuedTasksList() {
        List<Task> taskList = new LinkedList<>();
        for (Map.Entry<Integer, ConcurrentLinkedQueue<Task>> entry : this.multiLevelQueueMap.entrySet()) {
            taskList.addAll(entry.getValue());
        }
        return taskList;
    }

    /**
     * Gets position.
     *
     * @param task the task
     * @return the position
     */
    public int getPosition(Task task) {
        int positionInSamePriority = getPositionInSamePriority(task);

        if (positionInSamePriority == -1) {
            return -1;
        }

        int position = positionInSamePriority;

        for (int i = task.getPriority() + 1; i <= NO_OF_QUEUE_LEVELS; i++) {
            position += this.multiLevelQueueMap.get(i).size();
        }

        return position;
    }

    private int getPositionInSamePriority(Task task) {
        int position = 1;
        for (Task queuedTask : this.multiLevelQueueMap.get(task.getPriority())) {
            if (queuedTask.getId().equals(task.getId())) {
                return position;
            }
            position++;
        }
        return -1;
    }

    /**
     * Restores the tasks in the queue from backup.
     *
     * @param taskList list of tasks to be restored
     */
    public void restoreBackup(@NotNull List<Task> taskList) {
        for (Task task : taskList) {
            this.enqueue(task);
        }
    }

    /**
     * Returns the max time a task has been enqueued.
     *
     * @return max enqueue time.
     */
    public long getMaxTime() {
        long maxTime = 0;
        for (Map.Entry<Integer, ConcurrentLinkedQueue<Task>> entry : this.multiLevelQueueMap.entrySet()) {
            if ((entry.getValue()).peek() != null) {
                long queueMaxTime = System.currentTimeMillis() - (entry.getValue()).peek().getEnqueueTime();
                if (queueMaxTime > maxTime) {
                    maxTime = queueMaxTime;
                }
            }
        }
        return maxTime;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("->/");

        for (int i = NO_OF_QUEUE_LEVELS; i >= 1; i--) {
            for (Task task : this.multiLevelQueueMap.get(i)) {
                result.append(task.getId()).append(",");
            }
        }

        String strResult = result.toString();
        String end = strResult.substring(strResult.length() - 1).equalsIgnoreCase("/")
                ? strResult + "/<-" : strResult.substring(0, strResult.length() - 1) + "/<-";

        result.append(end);

        return result.toString();
    }
}
