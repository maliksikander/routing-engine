package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestMediaRoutingDomainRepository {

    @Autowired
    MediaRoutingDomainRepository repository;

    @Test
    public void test_repository() {
        repository.deleteAll();

        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID());
        System.out.println("mrd_id: " + mrd.getId().toString());
        mrd.setName("Name2");
        mrd.setDescription("Description2");
        mrd.setInterruptible(false);

        repository.save(mrd);
        System.out.println("---------------------------");
        System.out.println(repository.findByName("Name2").toString());
    }
}
