package com.ef.mediaroutingengine.services.redis;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Set;

public class RedisJsonDao<T> {
    private final RedisClient redisClient;
    private final String type;
    private final Class<T> clazz;

    /**
     * Constructor.
     *
     * @param redisClient the redis client to send commands to redis server.
     * @param type type of the objects in the collection.
     */
    @SuppressWarnings("unchecked")
    public RedisJsonDao(RedisClient redisClient, String type) {
        this.redisClient = redisClient;
        this.clazz = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        this.type = type;
    }

    /**
     * Inserts if not present, updates if present, and object on a specific key in Redis.
     *
     * @param id the id of the object to be saved.
     * @param value the object to be saved.
     * @return true if saved successfully, false otherwise.
     */
    public boolean save(String id, T value) {
        try {
            // Todo: Implement transaction for this
            if (this.redisClient.setJson(this.getKey(id), value)) {
                this.redisClient.SADD(type, id);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public T find(String id) {
        return this.redisClient.getJson(this.getKey(id), clazz);
    }

    /**
     * Returns the list of all objects in the collection.
     *
     * @return List of all objects in the collection.
     */
    public List<T> findAll() {
        Set<String> idSet = this.redisClient.SMEMBERS(this.type);
        String[] idArray = new String[idSet.size()];

        int i = 0;
        for (String id : idSet) {
            idArray[i] = this.getKey(id);
            i++;
        }
        return this.redisClient.multiGetJson(this.clazz, idArray);
    }

    public boolean updateField(String id, String path, Object value) {
        return this.redisClient.setJson(this.getKey(id), path, value);
    }

    private String getKey(String id) {
        return this.type + ":" + id;
    }
}
