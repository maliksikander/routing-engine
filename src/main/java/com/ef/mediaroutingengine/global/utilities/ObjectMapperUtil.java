package com.ef.mediaroutingengine.global.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * The type Object mapper util.
 */
public final class ObjectMapperUtil {
    /**
     * The constant objectMapper.
     */
    private static final ObjectMapper objectMapper = createObjectMapper();

    /**
     * Instantiates a new Object mapper util.
     */
    private ObjectMapperUtil() {

    }

    /**
     * Get object mapper.
     *
     * @return the object mapper
     */
    public static ObjectMapper get() {
        return objectMapper;
    }

    /**
     * Create object mapper.
     *
     * @return the object mapper
     */
    private static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .addModule(new ParameterNamesModule())
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
                .build();
    }
}
