package com.ef.mediaroutingengine.services.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Set;

public interface RedisClient {


    /**
     * Associate Object as JSON value to key.
     *
     * @param key    String
     * @param object Object
     */
    boolean setJson(String key, Object object);

    boolean setJson(String key, String path, Object object);

    boolean setJsonWithSet(String type, String id, Object object);

    /**
     * Returns the Object associated with the key. If the key does not exist, the special value nil
     * is returned. If the value stored by key is not of Object type, an error is returned, because
     * GET JSON can only be used to process give Object.
     *
     * @param key   String
     * @param clazz class type T
     * @param <T>   type T
     * @return T
     */
    <T> T getJson(String key, Class<T> clazz);

    <T> List<T> getJsonArray(String key, Class<T> clazz) throws JsonProcessingException;

    /**
     * Get the list Objects associated with the keys.  If any key does not exist, the special value
     * nil is returned.
     *
     * @param keys String
     * @param <T>  type T
     * @return T
     */
    <T> List<T> multiGetJson(Class<T> clazz, String... keys);

    /**
     * Delete an object associated given key.
     *
     * @param key String
     * @return Long
     */
    Long delJson(String key);

    boolean delJsonWithSet(String type, String id);

    /**
     * Adds one or more members to a set.
     *
     * @param key    String
     * @param member String
     */
    Long SADD(String key, String member);

    /**
     * Gets all the members in a set.
     *
     * @param key String
     * @return set of String
     */
    Set<String> SMEMBERS(String key);

    /**
     * Removes one or more members from a set.
     *
     * @param key    String
     * @param member String
     * @return Long
     */
    Long SREM(String key, String... member);


    /**
     * Associate value as String  to key.
     *
     * @param key   String
     * @param value String
     */
    void set(String key, String value);

    /**
     * Get value associated with key.
     *
     * @param key String
     * @return String
     */
    String get(String key);

    /**
     * Delete an value associated given key.
     *
     * @param key String
     * @return Long
     */

    Long del(String key);

    boolean exists(String key);
}




