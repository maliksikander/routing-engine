package com.ef.mediaroutingengine.global.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

/**
 * The type Redis json.
 */
public class RedisJson {
    /**
     * The constant objectMapper.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Instantiates a new Redis json.
     */
    private RedisJson() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * The enum Redis json command.
     */
    public enum Command implements ProtocolCommand {
        /**
         * Del command.
         */
        DEL("JSON.DEL"),
        /**
         * Get command.
         */
        GET("JSON.GET"),
        /**
         * Set command.
         */
        SET("JSON.SET"),
        /**
         * Multiple get command.
         */
        MGET("JSON.MGET"),
        /**
         * Type command.
         */
        TYPE("JSON.TYPE");
        /**
         * The Raw.
         */
        private final byte[] raw;

        /**
         * Instantiates a new Command.
         *
         * @param alt the alt
         */
        Command(String alt) {
            raw = SafeEncoder.encode(alt);
        }

        public byte[] getRaw() {
            return raw;
        }
    }

    public static byte[][] encode(String key, Object o) throws JsonProcessingException {
        return SafeEncoder.encodeMany(key, objectMapper.writeValueAsString(o));
    }

    /**
     * Encode byte [ ] [ ].
     *
     * @param key  the key
     * @param path the path
     * @param o    the o
     * @return the byte [ ] [ ]
     * @throws JsonProcessingException the json processing exception
     */
    public static byte[][] encode(String key, String path, Object o) throws JsonProcessingException {
        return SafeEncoder.encodeMany(key, path, objectMapper.writeValueAsString(o));
    }
}

