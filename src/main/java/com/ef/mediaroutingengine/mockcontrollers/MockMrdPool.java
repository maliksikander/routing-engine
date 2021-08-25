package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockMrdPool {
    private final MrdPool mrdPool;

    @Autowired
    public MockMrdPool(MrdPool mrdPool) {
        this.mrdPool = mrdPool;
    }

    @PutMapping("/mrd-pool/{id}")
    public String update(@RequestBody MediaRoutingDomain mediaRoutingDomain, @PathVariable UUID id) {
        if (mrdPool.contains(id)) {
            mediaRoutingDomain.setId(id);
            this.mrdPool.update(mediaRoutingDomain);
            return "Success";
        }
        return "Could not find MRD to update";
    }

    @GetMapping("/mrd-pool")
    public ResponseEntity<Object> retrieve() {
        return new ResponseEntity<>(this.mrdPool.findAll(), HttpStatus.OK);
    }
}
