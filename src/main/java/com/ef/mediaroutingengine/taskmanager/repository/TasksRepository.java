package com.ef.mediaroutingengine.taskmanager.repository;

import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskAgent;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.mediaroutingengine.global.redis.RedisClient;
import com.ef.mediaroutingengine.global.redis.RedisJsonDao;
import com.ef.mediaroutingengine.routing.model.AgentReqTimerEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The type Tasks repository.
 */
@Component
public class TasksRepository extends RedisJsonDao<Task> {

    /**
     * Instantiates a new Tasks repository.
     *
     * @param redisClient the redis client
     */
    @Autowired
    public TasksRepository(RedisClient redisClient) {
        super(redisClient, "task");
    }

    /**
     * Find all list.
     *
     * @return the list
     */
    public List<Task> findAll() {
        return this.findAll(2000);
    }

    /**
     * Find all by agent list.
     *
     * @param agentId the agent id
     * @return the list
     */
    public List<Task> findAllByAgent(String agentId) {
        return this.findAll().stream()
                .filter(t -> t.getAssignedTo() != null && t.getAssignedTo().getId().equals(agentId))
                .toList();
    }

    /**
     * Find all by conversation list.
     *
     * @param conversationId the conversation id
     * @return the list
     */
    public List<Task> findAllByConversation(String conversationId) {
        return this.findAll().stream()
                .filter(t -> t.getConversationId().equals(conversationId))
                .toList();
    }

    /**
     * Find all by mrd id list.
     *
     * @param mrdId the mrd id
     * @return the list
     */
    public List<Task> findAllByMrdId(String mrdId) {
        return this.findAll().stream()
                .filter(t -> t.getActiveMedia().stream().anyMatch(m -> m.getMrdId().equals(mrdId)))
                .toList();
    }

    /**
     * Find all by queue id list.
     *
     * @param queueId the queue id
     * @return the list
     */
    public List<Task> findAllByQueueId(String queueId) {
        return this.findAll().stream()
                .filter(t -> t.getActiveMedia().stream()
                        .anyMatch(m -> m.getQueue() != null && m.getQueue().getId().equals(queueId)))
                .toList();
    }

    /**
     * Find media by task media.
     *
     * @param taskId  the task id
     * @param mediaId the media id
     * @return the task media
     */
    public TaskMedia findMedia(String taskId, String mediaId) {
        for (TaskMedia media : this.findAllMedia(taskId)) {
            if (media.getId().equals(mediaId)) {
                return media;
            }
        }
        return null;
    }

    /**
     * Find all media list.
     *
     * @param taskId the task id
     * @return the list
     */
    public List<TaskMedia> findAllMedia(String taskId) {
        return this.findArrayField(taskId, ".activeMedia", TaskMedia.class);
    }

    /**
     * Find queued grouped by queue id list.
     *
     * @param conversationId the conversation id
     * @return the list
     */
    public Map<String, List<Task>> findQueuedGroupedByQueueId(String conversationId) {
        Map<String, List<Task>> result = new HashMap<>();

        for (Task task : this.findAllByConversation(conversationId)) {
            TaskMedia media = task.findMediaByState(TaskMediaState.QUEUED);
            if (media != null) {
                result.computeIfAbsent(media.getQueue().getId(), v -> new ArrayList<>());
                result.get(media.getQueue().getId()).add(task);
            }
        }

        return result;
    }

    /**
     * Update media state.
     *
     * @param taskId  the task id
     * @param mediaId the media id
     * @param state   the state
     */
    public void updateMediaState(String taskId, String mediaId, TaskMediaState state) {
        List<TaskMedia> medias = this.findAllMedia(taskId);

        for (TaskMedia media : medias) {
            if (media.getId().equals(mediaId)) {
                media.setState(state);
                this.updateField(taskId, ".activeMedia", medias);
                break;
            }
        }
    }

    /**
     * Reserve task.
     *
     * @param taskId  the task id
     * @param mediaId the media id
     * @param agent   the agent
     */
    public void reserve(String taskId, String mediaId, TaskAgent agent) {
        Task task = this.find(taskId);

        if (task != null) {
            TaskMedia taskMedia = task.findMediaBy(mediaId);

            if (taskMedia != null) {
                task.setAssignedTo(agent);
                taskMedia.setState(TaskMediaState.RESERVED);
                this.save(taskId, task);
            }
        }
    }

    /**
     * Update assigned to boolean.
     *
     * @param taskId     the task id
     * @param assignedTo the assigned to
     * @return the boolean
     */
    public boolean updateAssignedTo(String taskId, TaskAgent assignedTo) {
        return this.updateField(taskId, ".assignedTo", assignedTo);
    }

    public boolean updateActiveMedias(String taskId, List<TaskMedia> activeMedias) {
        return this.updateField(taskId, ".activeMedia", activeMedias);
    }

    public void saveAgentReqTimerEntity(String timerId, AgentReqTimerEntity entity) {
        this.redisClient.setJson(this.getAgentReqTimerKey(timerId), entity);
    }

    public AgentReqTimerEntity getAgentReqTimerEntity(String timerId) {
        return this.redisClient.getJson(this.getAgentReqTimerKey(timerId), AgentReqTimerEntity.class);
    }

    public void deleteAgentReqTimerEntity(String timerId) {
        this.redisClient.delJson(timerId);
    }

    private String getAgentReqTimerKey(String timerId) {
        return "agentRequestTimer:" + timerId;
    }
}
