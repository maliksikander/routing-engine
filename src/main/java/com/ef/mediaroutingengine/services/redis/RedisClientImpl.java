package com.ef.mediaroutingengine.services.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

@Service
public class RedisClientImpl implements RedisClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final JedisPool jedisPool;
    private static final String JSON_ROOT_PATH = ".";


    @Autowired
    public RedisClientImpl(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * Helper to check for an OK reply.
     *
     * @param str the reply string to "scrutinize"
     */
    private static void assertReplyOK(final String str) {
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
    public boolean setJson(String key, Object object) {
        return this.setJson(key, JSON_ROOT_PATH, object);
    }

    @Override
    public boolean setJson(String key, String path, Object object) {
        try (Jedis conn = getConnection()) {
            String value = objectMapper.writeValueAsString(object);
            conn.getClient().sendCommand(Command.SET, SafeEncoder.encodeMany(key, path, value));
            String status = conn.getClient().getStatusCodeReply();
            assertReplyOK(status);
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public <T> T getJson(String key, Class<T> clazz) {
        try (Jedis conn = getConnection()) {
            conn.getClient().sendCommand(Command.GET, SafeEncoder.encodeMany(key, JSON_ROOT_PATH));
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
    public <T> List<T> getJsonArray(String key, Class<T> clazz) throws JsonProcessingException {
        String response = null;
        try (Jedis conn = getConnection()) {
            conn.getClient().sendCommand(Command.GET, SafeEncoder.encodeMany(key, JSON_ROOT_PATH));
            response = conn.getClient().getBulkReply();
        }
        if (response != null) {
            assertReplyNotError(response);
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(response, type);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public <T> List<T> multiGetJson(Class<T> clazz, String... keys) {
        List<T> responseList = new ArrayList<>();
        try (Jedis conn = getConnection()) {
            String keysString = String.join(" ", keys);
            conn.getClient().sendCommand(Command.MGET, SafeEncoder.encodeMany(keysString, "."));
            List<String> objectsList = conn.getClient().getMultiBulkReply();

            if (objectsList != null) {
                for (String object : objectsList) {
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
            conn.getClient().sendCommand(Command.DEL, SafeEncoder.encodeMany(key, JSON_ROOT_PATH));
            return conn.getClient().getIntegerReply();
        }
    }

    @Override
    public Long SADD(String key, String member) {
        try (Jedis conn = getConnection()) {
            return conn.sadd(key, member);
        }
    }

    @Override
    public Set<String> SMEMBERS(String key) {
        try (Jedis conn = getConnection()) {
            return conn.smembers(key);
        }
    }

    @Override
    public Long SREM(String key, String... member) {
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
        assertReplyOK(status);
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

    private Jedis getConnection() {
        return this.jedisPool.getResource();
    }

    private enum Command implements ProtocolCommand {
        DEL("JSON.DEL"),
        GET("JSON.GET"),
        SET("JSON.SET"),
        MGET("JSON.MGET"),
        TYPE("JSON.TYPE");
        private final byte[] raw;

        Command(String alt) {
            raw = SafeEncoder.encode(alt);
        }

        public byte[] getRaw() {
            return raw;
        }
    }
}
