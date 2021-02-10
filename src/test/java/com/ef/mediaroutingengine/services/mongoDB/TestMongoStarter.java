package com.ef.mediaroutingengine.services.mongoDB;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestMongoStarter {
    @Autowired
    private MongoStarter mongoStarter;

    @Test
    public void testConnect_runsSuccessfully(){
        mongoStarter.connect();
    }

    @Test
    public void testCreate_runsSuccessfully(){
        mongoStarter.create();
    }
}
