package com.ef.mediaroutingengine;

import static org.junit.jupiter.api.Assertions.assertEquals;


import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.services.redis.RedisClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyTest {
    @Autowired
    RedisClient redisClient;
    @Test
    void myTest() throws JsonProcessingException {
        if(redisClient.getJSON("ahamd", String.class) == null) {
            System.out.println("KeyNull");
        } else {
            System.out.println("key not null");
        }
    }
}
