package com.ef.mediaroutingengine.global.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.util.Pool;
import redis.clients.jedis.util.SafeEncoder;

/**
 * The type Redis client.
 */
@Service
public class RedisClientImpl implements RedisClient {
    /**
     * The constant objectMapper.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * The constant JSON_ROOT_PATH.
     */
    private static final String JSON_ROOT_PATH = ".";
    /**
     * The Jedis pool.
     */
    private final Pool<Jedis> jedisPool;


    /**
     * Instantiates a new Redis client.
     *
     * @param jedisPool the jedis pool
     */
    @Autowired
    public RedisClientImpl(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Helper to check for an OK reply.
     *
     * @param str the reply string to "scrutinize"
     */
    private static void assertReplyOk(final String str) {
        if (!str.equals("OK")) {
            throw new RuntimeException(str);
        }
    }

    /**
     * Helper to check for errors and throw them as an exception.
     *
     * @param str the reply string to "analyze"
     * @throws RuntimeException exception
     */
    private static void assertReplyNotError(final String str) {
        if (str.startsWith("-ERR")) {
            throw new RuntimeException(str.substring(5));
        }
    }

    @Override
    public void setTransaction(Predicate<Transaction> function) {
        Transaction transaction = null;
        try (Jedis conn = getConnection()) {

            transaction = conn.multi();
            boolean isApplied = function.test(transaction);

            if (isApplied) {
                transaction.exec();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.discard();
            }
            e.printStackTrace();
        }
    }

    @Override
    public boolean setJson(String key, Object object) {
        return this.setJson(key, JSON_ROOT_PATH, object);
    }

    @Override
    public boolean setJson(String key, String path, Object object) {
        try (Jedis conn = getConnection()) {
            conn.getClient().sendCommand(RedisJson.Command.SET, RedisJson.encode(key, path, object));
            String status = conn.getClient().getStatusCodeReply();
            assertReplyOk(status);
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean setJsonWithSet(String type, String id, Object object) {
        Transaction transaction = null;
        try (Jedis conn = getConnection()) {
            transaction = conn.multi();
            transaction.sadd(type, id);
            transaction.sendCommand(RedisJson.Command.SET, RedisJson.encode(getKey(type, id), JSON_ROOT_PATH, object));
            transaction.exec();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.discard();
            }
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean setAllJsonForType(String type, List<String> idList, List<Object> objectList) {
        Transaction transaction = null;
        try (Jedis conn = getConnection()) {
            transaction = conn.multi();
            for (int i = 0; i < idList.size(); i++) {
                String id = idList.get(i);
                Object object = objectList.get(i);

                transaction.sadd(type, id);
                transaction.sendCommand(RedisJson.Command.SET,
                        RedisJson.encode(getKey(type, id), JSON_ROOT_PATH, object));
            }
            transaction.exec();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.discard();
            }
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sets multiple json object along with storing their keys in a set.
     *
     * @param type        the type of the field or the name of the set.
     * @param jsonObjects Map of keys and their correspondent json objects.
     * @return true if operation successful, false otherwise.
     */
    public boolean setMultiJsonWithSet(String type, Map<UUID, Object> jsonObjects) {
        Transaction transaction = null;
        try (Jedis conn = getConnection()) {
            transaction = conn.multi();
            for (Map.Entry<UUID, Object> entry : jsonObjects.entrySet()) {
                String id = entry.getKey().toString();
                transaction.sadd(type, id);
                transaction.sendCommand(RedisJson.Command.SET,
                        RedisJson.encode(getKey(type, id), JSON_ROOT_PATH, entry.getValue()));
            }
            transaction.exec();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.discard();
            }
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public <T> T getJson(String key, Class<T> clazz) {
        return this.getJson(key, JSON_ROOT_PATH, clazz);
    }

    @Override
    public <T> T getJson(String key, String path, Class<T> clazz) {
        try (Jedis conn = getConnection()) {
            conn.getClient().sendCommand(RedisJson.Command.GET, SafeEncoder.encodeMany(key, path));
            String response = conn.getClient().getBulkReply();
            if (response != null) {
                assertReplyNotError(response);
                return objectMapper.readValue(response, clazz);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> getJsonArray(String key, Class<T> clazz) {
        return this.getJsonArray(key, JSON_ROOT_PATH, clazz);
    }

    @Override
    public <T> List<T> getJsonArray(String key, String path, Class<T> clazz) {
        String response;
        try (Jedis conn = getConnection()) {
            conn.getClient().sendCommand(RedisJson.Command.GET, SafeEncoder.encodeMany(key, path));
            response = conn.getClient().getBulkReply();
            if (response != null) {
                assertReplyNotError(response);
                JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
                return objectMapper.readValue(response, type);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public <T> List<T> multiGetJson(Class<T> clazz, String... keys) {
        if (keys.length == 0) {
            return new ArrayList<>();
        }

        List<T> responseList = new ArrayList<>();
        String[] args = Stream
                .of(keys, new String[] {JSON_ROOT_PATH})
                .flatMap(Stream::of)
                .toArray(String[]::new);

        try (Jedis conn = getConnection()) {
            List<String> rep;
            conn.getClient().sendCommand(RedisJson.Command.MGET, args);
            rep = conn.getClient().getMultiBulkReply();
            if (rep != null) {
                for (String object : rep) {
                    responseList.add(objectMapper.readValue(object, clazz));
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return responseList;
    }

    @Override
    public Long delJson(String key) {
        try (Jedis conn = getConnection()) {
            conn.getClient().sendCommand(RedisJson.Command.DEL, SafeEncoder.encodeMany(key, JSON_ROOT_PATH));
            return conn.getClient().getIntegerReply();
        }
    }

    @Override
    public boolean delJsonWithSet(String type, String id) {
        Transaction transaction = null;
        try (Jedis conn = getConnection()) {
            transaction = conn.multi();
            transaction.sendCommand(RedisJson.Command.DEL, SafeEncoder.encodeMany(getKey(type, id), JSON_ROOT_PATH));
            transaction.srem(type, id);
            transaction.exec();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.discard();
            }
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delAllJsonForType(String type) {
        Transaction transaction = null;
        try (Jedis conn = getConnection()) {
            Set<String> idList = conn.smembers(type);
            if (idList == null) {
                return false;
            }
            transaction = conn.multi();
            for (String id : idList) {
                transaction.sendCommand(RedisJson.Command.DEL,
                        SafeEncoder.encodeMany(getKey(type, id), JSON_ROOT_PATH));
            }
            transaction.del(type);
            transaction.exec();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.discard();
            }
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Long setAdd(String key, String member) {
        try (Jedis conn = getConnection()) {
            return conn.sadd(key, member);
        }
    }

    @Override
    public Set<String> setMembers(String key) {
        try (Jedis conn = getConnection()) {
            return conn.smembers(key);
        }
    }

    @Override
    public Long setRem(String key, String... member) {
        try (Jedis conn = getConnection()) {
            return conn.srem(key, member);
        }
    }

    @Override
    public void set(String key, String value) {
        String status;
        try (Jedis conn = getConnection()) {
            status = conn.set(key, value);
        }
        assertReplyOk(status);
    }

    @Override
    public String get(String key) {
        try (Jedis conn = getConnection()) {
            return conn.get(key);
        }
    }

    @Override
    public Long del(String key) {
        try (Jedis conn = getConnection()) {

            return conn.del(key);
        }
    }

    @Override
    public boolean exists(String key) {
        try (Jedis conn = getConnection()) {
            return conn.exists(key);
        }
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params) {
        try (Jedis conn = getConnection()) {
            return conn.scan(cursor, params);
        }
    }

    /**
     * Gets connection.
     *
     * @return the connection
     */
    @Override
    public Jedis getConnection() {
        return this.jedisPool.getResource();
    }

    /**
     * Gets key.
     *
     * @param type the type
     * @param id   the id
     * @return the key
     */
    private String getKey(String type, String id) {
        return type + ":" + id;
    }
}
