package com.ef.mediaroutingengine.services.queue;

import com.ef.mediaroutingengine.model.Task;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
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
    private final int noOfQueueLevels = 11;

    /**
     * Instantiates a new Priority queue.
     */
    public PriorityQueue() {
        this.multiLevelQueueMap = new ConcurrentHashMap<>();
        for (int i = this.noOfQueueLevels; i >= 1; i--) {
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
        if (task.getPriority() > 10) {
            task.setPriority(10);
        } else if (task.getPriority() < 1) {
            task.setPriority(1);
        }
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
        for (int i = this.noOfQueueLevels; i >= 1; i--) {
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
        for (int i = this.noOfQueueLevels; i >= 1; i--) {
            size = size + this.multiLevelQueueMap.get(i).size();
        }
        return size;
    }

    /**
     * Get task service.
     *
     * @param index the index of task service
     * @return the task service
     */
    public Task get(int index) {
        // TODO: implement this method
        return null;
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
        for (int i = this.noOfQueueLevels; i >= 1; i--) {
            Queue<Task> queue = this.multiLevelQueueMap.get(i);
            int k = 0;
            for (Iterator<?> it = queue.iterator(); it.hasNext(); ) {
                Task iter = (Task) it.next();
                System.out.println(iter);
                if (iter.getId().toString().equals(taskId)) {
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
            for (Iterator<?> it = entry.getValue().iterator(); it.hasNext(); ) {
                Task iter = (Task) it.next();
                if (iter.getId().toString().equals(taskId)) {
                    return iter;
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
    public boolean taskExists(UUID taskId) {
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
            Iterator it = entry.getValue().iterator();
            while (it.hasNext()) {
                Task i = (Task) it.next();
                if (i.getId().equals(task.getId())) {
                    entry.getValue().remove(i);
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
        for (Map.Entry entry : this.multiLevelQueueMap.entrySet()) {
            if (((ConcurrentLinkedQueue) entry.getValue()).peek() != null) {
                long queueMaxTime = System.currentTimeMillis()
                        - ((ConcurrentLinkedQueue<Task>) entry.getValue()).peek().getEnqueueTime();
                if (queueMaxTime > maxTime) {
                    maxTime = queueMaxTime;
                }
            }
        }
        return maxTime;
    }

    @Override
    public String toString() {
        String result = "->/";
        for (int i = this.noOfQueueLevels; i >= 1; i--) {
            for (Iterator<?> it = this.multiLevelQueueMap.get(i).iterator(); it.hasNext(); ) {
                result = result + ((Task) it.next()).getId() + ",";
            }
        }
        result = result.substring(result.length() - 1).equalsIgnoreCase("/")
                ? result + "/<-"
                : result.substring(0, result.length() - 1) + "/<-";
        return result;
    }
}
