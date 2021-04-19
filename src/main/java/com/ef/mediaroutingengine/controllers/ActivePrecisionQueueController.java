package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.services.redis.RedisClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActivePrecisionQueueController {
    private final RedisClient redisClient;

    public ActivePrecisionQueueController(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/active-precision-queues")
    public ResponseEntity<Object> retrieve() throws JsonProcessingException {
        List<PrecisionQueue> precisionQueues = redisClient.getJSON("activePrecisionQueues", ArrayList.class);
        return new ResponseEntity<>(precisionQueues, HttpStatus.OK);
    }
}
