package com.ef.mediaroutingengine.services.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Set;

public interface RedisClient {


    /**
     * Associate Object as JSON value to key
     *
     * @param key
     * @param object
     */
    public void setJSON(String key, Object object) throws Exception;


    /**
     * Returns the Object associated with the key. If the key does not exist, the special value nil
     * is returned. If the value stored by key is not of Object type, an error is returned, because
     * GET JSON can only be used to process give Object.
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getJSON(String key, Class<T> clazz) throws JsonProcessingException;

    /**
     * Get the list Objects associated with the keys.  If any key does not exist, the special value
     * nil is returned.
     *
     * @param keys
     * @param <T>
     * @return
     */
    public <T> List<T> mgetJSON(Class<T> clazz, String... keys) throws JsonProcessingException;

    /**
     * Delete an object associated given key.
     *
     * @param key
     * @return
     */
    Long delJSON(String key);

    /**
     * Adds one or more members to a set
     *
     * @param key
     * @param member
     */
    public Long SADD(String key, String member);

    /**
     * Gets all the members in a set
     *
     * @param key
     * @return
     */
    public Set<String> SMEMBERS(String key);

    /**
     * Removes one or more members from a set
     *
     * @param key
     * @param member
     */
    public Long SREM(String key, String... member);


    /**
     * Associate value as String  to key
     *
     * @param key
     * @param value
     */
    public void set(String key, String value);

    /**
     * Get value associated with key
     *
     * @param key
     * @return
     */
    public String get(String key);

    /**
     * Delete an value associated given key.
     *
     * @param key
     * @return
     */

    public Long del(String key);


}




