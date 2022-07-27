package com.ef.mediaroutingengine.taskmanager.pool;


import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Tasks pool.
 */
@Service
public class TasksPool {

    /**
     * Contains all tasks in the pool.
     */
    private final List<Task> allTasks;


    /**
     * Default constructor.
     */
    @Autowired
    public TasksPool() {
        this.allTasks = new CopyOnWriteArrayList<>();
    }

    /**
     * Contains boolean.
     *
     * @param task the task
     * @return the boolean
     */
    private boolean contains(Task task) {
        for (Task element : this.allTasks) {
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
            this.allTasks.add(task);
        }
    }

    /**
     * Remove boolean.
     *
     * @param task the task
     * @return the boolean
     */
    public boolean remove(Task task) {
        return this.allTasks.remove(task);
    }

    /**
     * Find all list.
     *
     * @return the list
     */
    public List<Task> findAll() {
        return this.allTasks;
    }

    /**
     * Returns task by task-id from the tasks pool.
     *
     * @param taskId id of the task to find
     * @return TaskService object if found, null otherwise
     */
    public Task findById(UUID taskId) {
        for (Task task : this.allTasks) {
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
    public Task findInProcessTaskFor(UUID conversationId) {
        return this.allTasks.stream()
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
     * Find by conversation id list.
     *
     * @param conversationId the conversation id
     * @return the list
     */
    public List<Task> findByConversationId(UUID conversationId) {
        List<Task> result = new ArrayList<>();
        for (Task task : this.allTasks) {
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
        this.allTasks.forEach(task -> {
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
        this.allTasks.forEach(task -> {
            if (task.getQueue() != null && task.getQueue().equals(id)) {
                taskList.add(task);
            }
        });
        return taskList;
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return this.allTasks.size();
    }
}
