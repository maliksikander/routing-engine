package com.ef.mediaroutingengine.services.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
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
    public void setJSON(String key, Object object) throws JsonProcessingException {

        String status;
        try (Jedis conn = getConnection()) {

            conn.getClient()
                    .sendCommand(Command.SET, SafeEncoder.encodeMany(key, Path.ROOT_PATH.toString(),
                            objectMapper.writeValueAsString(object)));
            status = conn.getClient().getStatusCodeReply();
        }
        assertReplyOK(status);

    }

    @Override
    public <T> T getJSON(String key, Class<T> clazz) throws JsonProcessingException {

        String response = null;
        try (Jedis conn = getConnection()) {
            conn.getClient().sendCommand(Command.GET,
                    SafeEncoder.encodeMany(key, Path.ROOT_PATH.toString()));
            response = conn.getClient().getBulkReply();
        }
        if (response != null) {
            assertReplyNotError(response);
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
            };
            return objectMapper.readValue(response, clazz);
        } else {
            return null;
        }
    }

    @Override
    public <T> List<T> getJsonArray(String key, Class<T> clazz) throws JsonProcessingException {
        String response = null;
        try (Jedis conn = getConnection()) {
            conn.getClient().sendCommand(Command.GET,
                    SafeEncoder.encodeMany(key, Path.ROOT_PATH.toString()));
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
    public <T> List<T> mgetJSON(Class<T> clazz, String... keys) throws JsonProcessingException {

        List<String> objectsList = null;
        List<T> responseList = new ArrayList<>();
        try (Jedis conn = getConnection()) {

            conn.getClient().sendCommand(Command.MGET, SafeEncoder.encodeMany(keys));
            objectsList = conn.getClient().getMultiBulkReply();
        }

        if (objectsList != null && !objectsList.isEmpty()) {
            for (String object : objectsList) {
                responseList.add(objectMapper.readValue(object, clazz));
            }
        }
        return responseList;
    }

    @Override
    public Long delJSON(String key) {
        try (Jedis conn = getConnection()) {
            conn.getClient().sendCommand(Command.DEL,
                    SafeEncoder.encodeMany(key, Path.ROOT_PATH.toString()));
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

//    @Override
//    public void setJSON(String key, Object object ) {
//
//
//        List<byte[]> args = new ArrayList<>(4);
//
//        args.add(SafeEncoder.encode(key));
//        args.add(SafeEncoder.encode(Path.ROOT_PATH.toString()));
//        args.add(SafeEncoder.encode(gson.toJson(object)));
//        String status;
//        try (Jedis conn = getConnection()) {
//            conn.getClient()
//                    .sendCommand(Command.SET, args.toArray(new byte[args.size()][]));
//            status = conn.getClient().getStatusCodeReply();
//        }
//        assertReplyOK(status);
//
//    }
//
//    @Override
//    public <T> T getJSON(String key, Class<T> clazz) {
//        return null;
//    }
//
//    @Override
//    public <T> List<T> mgetJSON(String... keys) {
//        return null;
//    }
//
//
//    @Override
//    public <T> T getJSON(String key, Class<T> clazz, Path... paths) {
//
//        byte[][] args = new byte[1 + paths.length][];
//        int i=0;
//        args[i] = SafeEncoder.encode(key);
//        for (Path p :paths) {
//            args[++i] = SafeEncoder.encode(p.toString());
//        }
//
//        String rep;
//        try (Jedis conn = getConnection()) {
//            conn.getClient().sendCommand(Command.GET, args);
//            rep = conn.getClient().getBulkReply();
//        }
//
//        if(rep!=null){
//            assertReplyNotError(rep);
//            return gson.fromJson(rep, clazz);
//        }
//        else
//            return null;
//    }
//
////    @Override
////    public Long del(String key) {
////        Long rep;
////        try (Jedis conn = getConnection()) {
////            rep=conn.del(key);
////        }
////        return rep;
////    }
//
//    @Override
//    public Long delJSON(String key) {
//
//        byte[][] args = new byte[2][];
//        args[0] = SafeEncoder.encode(key);
//        args[1] = SafeEncoder.encode(Path.ROOT_PATH.toString());
//
//        try (Jedis conn = getConnection()) {
//            conn.getClient().sendCommand(Command.DEL, args);
//            return conn.getClient().getIntegerReply();
//        }
//    }
//
//    @Override
//    public void SADD(String key, String member) {
//
//    }
//
//    @Override
//    public Set<String> SMEMBERS(String key) {
//        return null;
//    }
//
//    @Override
//    public void SREM(String key, String... member) {
//
//    }
//
//    private Jedis getConnection() {
//        return this.jedisPool.getResource();
//    }
//
//    /**
//     * Existential modifier for the set command, by default we don't care
//     */
//    private enum ExistenceModifier implements ProtocolCommand {
//        DEFAULT(""),
//        NOT_EXISTS("NX"),
//        MUST_EXIST("XX");
//        private final byte[] raw;
//
//        ExistenceModifier(String alt) {
//            raw = SafeEncoder.encode(alt);
//        }
//
//        public byte[] getRaw() {
//            return raw;
//        }
//    }
//
//    private enum Command implements ProtocolCommand {
//        DEL("JSON.DEL"),
//        GET("JSON.GET"),
//        SET("JSON.SET"),
//        TYPE("JSON.TYPE");
//        private final byte[] raw;
//
//        Command(String alt) {
//            raw = SafeEncoder.encode(alt);
//        }
//
//        public byte[] getRaw() {
//            return raw;
//        }
//    }
}
