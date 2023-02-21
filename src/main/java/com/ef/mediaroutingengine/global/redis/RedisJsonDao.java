package com.ef.mediaroutingengine.global.redis;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * The type Redis json dao.
 *
 * @param <T> the type parameter
 */
public class RedisJsonDao<T> {
    /**
     * The Redis client.
     */
    private final RedisClient redisClient;
    /**
     * The Type.
     */
    private final String type;
    /**
     * The Clazz.
     */
    private final Class<T> clazz;

    /**
     * Constructor.
     *
     * @param redisClient the redis client to send commands to redis server.
     * @param type        type of the objects in the collection.
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
     * @param id    the id of the object to be saved.
     * @param value the object to be saved.
     * @return true if saved successfully, false otherwise.
     */
    public boolean save(String id, T value) {
        return this.redisClient.setJsonWithSet(this.type, id, value);
    }

    /**
     * Saves all objects in the key-value map.
     *
     * @param keyValueMap map of keys and values to be stored
     * @return true if operation successful, false otherwise.
     */
    public boolean saveAllByKeyValueMap(Map<String, T> keyValueMap, int pageSize) {
        List<Map.Entry<String, T>> entries = new ArrayList<>(keyValueMap.entrySet());
        int totalEntries = entries.size();

        for (int i = 0; i < totalEntries; i += pageSize) {
            int end = Math.min(totalEntries, i + pageSize);
            List<Map.Entry<String, T>> page = entries.subList(i, end);

            List<String> idList = new ArrayList<>();
            List<Object> objectList = new ArrayList<>();
            for (Map.Entry<String, T> entry : page) {
                idList.add(entry.getKey());
                objectList.add(entry.getValue());
            }

            if (!this.redisClient.setAllJsonForType(this.type, idList, objectList)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Find t.
     *
     * @param id the id
     * @return the t
     */
    public T find(String id) {
        return this.redisClient.getJson(this.getKey(id), clazz);
    }

    /**
     * Returns the list of all objects in the collection.
     *
     * @return List of all objects in the collection.
     */
    public List<T> findAll(int pageSize) {
        List<T> resultList = new ArrayList<>();
        String cursor = "0";
        String pattern = this.type + ":*";

        do {
            ScanResult<String> scanResult = this.redisClient.scan(cursor, new ScanParams().match(pattern)
                    .count(pageSize));
            cursor = scanResult.getCursor();
            List<String> keys = scanResult.getResult();

            if (!keys.isEmpty()) {
                resultList.addAll(this.redisClient.multiGetJson(this.clazz, keys.toArray(new String[0])));
            }
        } while (!cursor.equals("0"));

        return resultList;
    }

    /**
     * Update field boolean.
     *
     * @param id    the id
     * @param path  the path
     * @param value the value
     * @return the boolean
     */
    public boolean updateField(String id, String path, Object value) {
        return this.redisClient.setJson(this.getKey(id), path, value);
    }

    /**
     * Delete by id boolean.
     *
     * @param id the id
     * @return the boolean
     */
    public boolean deleteById(String id) {
        return this.redisClient.delJsonWithSet(this.type, id);
    }

    /**
     * Delete all boolean.
     *
     * @return the boolean
     */
    public boolean deleteAll() {
        return this.redisClient.delAllJsonForType(this.type);
    }

    /**
     * Gets key.
     *
     * @param id the id
     * @return the key
     */
    private String getKey(String id) {
        return this.type + ":" + id;
    }
}
