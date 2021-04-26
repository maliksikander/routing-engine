package com.ef.mediaroutingengine.services.queue;

import com.ef.mediaroutingengine.model.Pair;
import com.ef.mediaroutingengine.model.TaskService;
import com.ef.mediaroutingengine.repositories.PriorityLabelsPool;
import java.util.HashMap;
import java.util.Iterator;
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
    private final Map<Integer, ConcurrentLinkedQueue<TaskService>> multiLevelQueueMap;
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
     * @param taskService the task service
     * @return the boolean
     */
    public boolean enqueue(@NotNull TaskService taskService) {
        if (taskService.getPriority() > 10) {
            taskService.setPriority(10);
        } else if (taskService.getPriority() < 1) {
            taskService.setPriority(1);
        }
        return this.multiLevelQueueMap.get(taskService.getPriority()).offer(taskService);
    }

    /**
     * Dequeue task service.
     *
     * @param poll Task will be removed from queue if poll is true (queue poll operation),
     *             task will not be removed from queue otherwise (queue peek operation)
     * @return the task service in both cases (poll or peak), returns null if task not found
     */
    public TaskService dequeue(boolean poll) {
        for (int i = this.noOfQueueLevels; i >= 1; i--) {
            if (this.multiLevelQueueMap.get(i).size() > 0) {
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
    public TaskService get(int index) {
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
            Queue<TaskService> queue = this.multiLevelQueueMap.get(i);
            int k = 0;
            for (Iterator<?> it = queue.iterator(); it.hasNext(); ) {
                TaskService iter = (TaskService) it.next();
                System.out.println(iter);
                if (iter.getId().equalsIgnoreCase(taskId)) {
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
     * Get task task service.
     *
     * @param taskId the task id
     * @return the task service
     */
    public TaskService getTask(String taskId) {
        for (Map.Entry<Integer, ConcurrentLinkedQueue<TaskService>> entry : this.multiLevelQueueMap.entrySet()) {
            for (Iterator<?> it = entry.getValue().iterator(); it.hasNext(); ) {
                TaskService iter = (TaskService) it.next();
                if (iter.getId().equalsIgnoreCase(taskId)) {
                    return iter;
                }
            }
        }
        return null;
    }

    /**
     * Task exists boolean.
     *
     * @param taskService the task service
     * @return the boolean
     */
    public boolean taskExists(@NotNull TaskService taskService) {
        return taskExists(taskService.getId());
    }

    /**
     * Task exists boolean.
     *
     * @param taskId the task id
     * @return the boolean
     */
    public boolean taskExists(String taskId) {
        for (Map.Entry<Integer, ConcurrentLinkedQueue<TaskService>> entry : this.multiLevelQueueMap.entrySet()) {
            for (TaskService taskService1 : entry.getValue()) {
                if (taskId.equalsIgnoreCase(taskService1.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove boolean.
     *
     * @param taskService the task service
     * @return the boolean
     */
    public boolean remove(@NotNull TaskService taskService) {
        for (Map.Entry<Integer, ConcurrentLinkedQueue<TaskService>> entry : this.multiLevelQueueMap.entrySet()) {
            Iterator it = entry.getValue().iterator();
            while (it.hasNext()) {
                TaskService i = (TaskService) it.next();
                if (i.getId().equalsIgnoreCase(taskService.getId())) {
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
    public List<TaskService> getEnqueuedTasksList() {
        List<TaskService> taskServiceList = new LinkedList<>();
        for (Map.Entry<Integer, ConcurrentLinkedQueue<TaskService>> entry : this.multiLevelQueueMap.entrySet()) {
            for (TaskService taskService1 : entry.getValue()) {
                taskServiceList.add(taskService1);
            }
        }
        return taskServiceList;
    }

    /**
     * Restores the tasks in the queue from backup.
     *
     * @param taskServiceList list of tasks to be restored
     */
    public void restoreBackup(@NotNull List<TaskService> taskServiceList) {
        for (TaskService taskService : taskServiceList) {
            this.enqueue(taskService);
        }
    }

    /**
     * Returns the label stats of the queue.
     *
     * @return map of label stats
     */
    public Map<String, Pair> getLabelStats() {
        Map<String, Integer> labelStats = new HashMap<>();
        for (int i = this.noOfQueueLevels; i >= 1; i--) {
            for (Iterator<?> it = this.multiLevelQueueMap.get(i).iterator(); it.hasNext(); ) {
                String selectedPriorityLabel = ((TaskService) it.next()).getSelectedPriorityLabel();
                if ("".equalsIgnoreCase(selectedPriorityLabel)) {
                    continue;
                }
                if (labelStats.containsKey(selectedPriorityLabel)) {
                    labelStats.put(selectedPriorityLabel, (labelStats.get(selectedPriorityLabel).intValue()) + 1);
                } else {
                    labelStats.put(selectedPriorityLabel, 1);
                }
            }
        }
        Map<String, Pair> labelsAndMaxTimeStats = new HashMap<>();
        for (Map.Entry entry : labelStats.entrySet()) {
            labelsAndMaxTimeStats.put(entry.getKey().toString(),
                    new Pair(
                            entry.getValue(),
                            (this.multiLevelQueueMap.get(PriorityLabelsPool.getInstance()
                                    .getPriorityLabel(entry.getKey().toString().toUpperCase())
                                    .getPriority()).peek() != null) ? (System.currentTimeMillis()
                                    - this.multiLevelQueueMap.get(PriorityLabelsPool.getInstance()
                                    .getPriorityLabel(entry.getKey().toString().toUpperCase())
                                    .getPriority()).peek().getEnqueueTime()) : 0
                    ));
        }
        return labelsAndMaxTimeStats;
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
                        - ((ConcurrentLinkedQueue<TaskService>) entry.getValue()).peek().getEnqueueTime();
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
                result = result + ((TaskService) it.next()).getId() + ",";
            }
        }
        result = result.substring(result.length() - 1).equalsIgnoreCase("/")
                ? result + "/<-"
                : result.substring(0, result.length() - 1) + "/<-";
        return result;
    }
}
