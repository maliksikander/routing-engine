package com.ef.mediaroutingengine.taskmanager.pool;


import static java.util.stream.Collectors.groupingBy;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * The type Tasks pool.
 */
@Service
public class TasksPool {

    /**
     * Contains all tasks in the pool.
     */
    private final List<Task> pool = new CopyOnWriteArrayList<>();

    public void loadFrom(List<TaskDto> taskDtoList) {
        this.pool.clear();
        taskDtoList.forEach(t -> this.pool.add(Task.getInstanceFrom(t)));
    }

    /**
     * Contains boolean.
     *
     * @param task the task
     * @return the boolean
     */
    private boolean contains(Task task) {
        for (Task element : this.pool) {
            if (element.getId().equals(task.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add.
     *
     * @param task the task
     */
    public void add(Task task) {
        if (!this.contains(task)) {
            this.pool.add(task);
        }
    }

    /**
     * Remove boolean.
     *
     * @param task the task
     * @return the boolean
     */
    public boolean remove(Task task) {
        return this.pool.remove(task);
    }

    /**
     * Find all list.
     *
     * @return the list
     */
    public List<Task> findAll() {
        return this.pool;
    }

    /**
     * Returns task by task-id from the tasks pool.
     *
     * @param taskId id of the task to find
     * @return TaskService object if found, null otherwise
     */
    public Task findById(String taskId) {
        for (Task task : this.pool) {
            if (task.getId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Find in process task for task.
     *
     * @param conversationId the conversation id
     * @return the task
     */
    public Task findInProcessTaskFor(String conversationId) {
        return this.pool.stream()
                .filter(t -> {
                    Enums.TaskStateName stateName = t.getTaskState().getName();
                    return t.getTopicId().equals(conversationId)
                            && (stateName.equals(Enums.TaskStateName.QUEUED)
                            || stateName.equals(Enums.TaskStateName.RESERVED));
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Find queued grouped by queue id map.
     *
     * @param conversationId the conversation id
     * @return the map
     */
    public Map<String, List<Task>> findQueuedGroupedByQueueId(String conversationId) {
        return this.pool.stream()
                .filter(t -> t.getTopicId().equals(conversationId)
                        && (t.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)))
                .collect(groupingBy(t -> t.getQueue().getId()));
    }

    /**
     * Find by conversation id list.
     *
     * @param conversationId the conversation id
     * @return the list
     */
    public List<Task> findByConversationId(String conversationId) {
        List<Task> result = new ArrayList<>();
        for (Task task : this.pool) {
            if (task.getTopicId().equals(conversationId)) {
                result.add(task);
            }
        }
        return result;
    }

    /**
     * Find tasks by mrd id.
     *
     * @param mrdId the mrd id
     * @return the list
     */
    public List<Task> findByMrdId(String mrdId) {
        List<Task> taskList = new ArrayList<>();
        this.pool.forEach(task -> {
            if (task.getMrd().getId().equals(mrdId)) {
                taskList.add(task);
            }
        });
        return taskList;
    }

    /**
     * Find by queue id list.
     *
     * @param id the id
     * @return the list
     */
    public List<Task> findByQueueId(String id) {
        List<Task> taskList = new ArrayList<>();
        this.pool.forEach(task -> {
            if (task.getQueue() != null && task.getQueue().getId().equals(id)) {
                taskList.add(task);
            }
        });
        return taskList;
    }

    /**
     * Find all push tasks list.
     *
     * @return the list
     */
    public List<Task> findAllQueuedTasks() {
        return this.pool.stream()
                .filter(t -> t.getType().getMode().equals(Enums.TaskTypeMode.QUEUE)
                        && t.getTaskState().getName().equals(Enums.TaskStateName.QUEUED))
                .collect(Collectors.toList());
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return this.pool.size();
    }
}
