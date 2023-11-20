package com.ef.mediaroutingengine.taskmanager.repository;

import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.global.redis.RedisClient;
import com.ef.mediaroutingengine.global.redis.RedisJson;
import com.ef.mediaroutingengine.global.redis.RedisJsonDao;
import com.ef.mediaroutingengine.routing.model.AgentReqTimerEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.util.SafeEncoder;

/**
 * The type Tasks repository.
 */
@Component
public class TasksRepository extends RedisJsonDao<Task> {
    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(TasksRepository.class);

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
     * Insert a new task.
     *
     * @param task the task to be inserted
     */
    public void insert(Task task) {
        Transaction transaction = null;
        try (Jedis conn = redisClient.getConnection()) {
            transaction = conn.multi();

            // Add taskId to a set of task ids of this conversation
            transaction.sadd(getConversationKey(task.getConversationId()), task.getId());
            // Add taskId to set of all task ids
            transaction.sadd(type, task.getId());
            // Save Task as a RedisJson Object
            transaction.sendCommand(RedisJson.Command.SET, RedisJson.encode(getKey(task.getId()), ".", task));

            transaction.exec();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.discard();
            }
            logger.error(ExceptionUtils.getMessage(e));
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public boolean save(String id, Task value) {
        logger.warn("Method not supported in TaskRepository, use insert to insert a task, update to update a task");
        return false;
    }

    /**
     * Update boolean.
     *
     * @param task the task
     * @return the boolean
     */
    public boolean update(Task task) {
        return super.save(task.getId(), task);
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
    public List<Task> findAllByConversationId(String conversationId) {
        String[] keys;

        try (Jedis conn = redisClient.getConnection()) {
            String conversationKey = getConversationKey(conversationId);
            keys = conn.smembers(conversationKey).stream().map(this::getKey).toArray(String[]::new);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ArrayList<>();
        }

        return redisClient.multiGetJson(Task.class, keys);
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
     * Find queued grouped by queue id list.
     *
     * @param conversationId the conversation id
     * @return the list
     */
    public Map<String, List<Task>> findQueuedGroupedByQueueId(String conversationId) {
        Map<String, List<Task>> result = new HashMap<>();

        for (Task task : this.findAllByConversationId(conversationId)) {
            TaskMedia media = task.findMediaByState(TaskMediaState.QUEUED);
            if (media != null) {
                result.computeIfAbsent(media.getQueue().getId(), v -> new ArrayList<>());
                result.get(media.getQueue().getId()).add(task);
            }
        }

        return result;
    }

    /**
     * Update active medias.
     *
     * @param taskId       the task id
     * @param activeMedias the active medias
     */
    public void updateActiveMedias(String taskId, List<TaskMedia> activeMedias) {
        this.updateField(taskId, ".activeMedia", activeMedias);
    }

    /**
     * Update state.
     *
     * @param taskId the task id
     * @param state  the state
     */
    public void updateState(String taskId, TaskState state) {
        this.updateField(taskId, ".state", state);
    }

    /**
     * Delete.
     *
     * @param task the task
     */
    public void delete(Task task) {
        Transaction transaction = null;
        try (Jedis conn = redisClient.getConnection()) {
            transaction = conn.multi();

            // Delete Task Json Object in Redis
            transaction.sendCommand(RedisJson.Command.DEL, SafeEncoder.encodeMany(getKey(task.getId()), "."));
            // Remove taskId from set of all task ids
            transaction.srem(type, task.getId());
            // Remove taskId from set of task ids of this conversation
            transaction.srem(getConversationKey(task.getConversationId()), task.getId());

            transaction.exec();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.discard();
            }
            logger.error(ExceptionUtils.getMessage(e));
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public boolean deleteById(String id) {
        logger.warn("Method not supported in TaskRepository, use delete(Task task) method instead");
        return false;
    }

    /**
     * Save agent req timer entity.
     *
     * @param timerId the timer id
     * @param entity  the entity
     */
    public void saveAgentReqTimerEntity(String timerId, AgentReqTimerEntity entity) {
        this.redisClient.setJson(this.getAgentReqTimerKey(timerId), entity);
    }

    /**
     * Gets agent req timer entity.
     *
     * @param timerId the timer id
     * @return the agent req timer entity
     */
    public AgentReqTimerEntity getAgentReqTimerEntity(String timerId) {
        return this.redisClient.getJson(this.getAgentReqTimerKey(timerId), AgentReqTimerEntity.class);
    }

    /**
     * Delete agent req timer entity.
     *
     * @param timerId the timer id
     */
    public void deleteAgentReqTimerEntity(String timerId) {
        this.redisClient.delJson(this.getAgentReqTimerKey(timerId));
    }

    /**
     * Gets agent req timer key.
     *
     * @param timerId the timer id
     * @return the agent req timer key
     */
    private String getAgentReqTimerKey(String timerId) {
        return "agentRequestTimer:" + timerId;
    }

    /**
     * Gets conversation key.
     *
     * @param conversationId the conversation id
     * @return the conversation key
     */
    private String getConversationKey(String conversationId) {
        return "taskConversation:" + conversationId;
    }
}
