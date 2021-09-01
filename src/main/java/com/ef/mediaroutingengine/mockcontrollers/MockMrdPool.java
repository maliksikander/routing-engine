package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Mock mrd pool.
 */
@RestController
public class MockMrdPool {
    /**
     * The Mrd pool.
     */
    private final MrdPool mrdPool;

    /**
     * Instantiates a new Mock mrd pool.
     *
     * @param mrdPool the mrd pool
     */
    @Autowired
    public MockMrdPool(MrdPool mrdPool) {
        this.mrdPool = mrdPool;
    }

    /**
     * Update string.
     *
     * @param mediaRoutingDomain the media routing domain
     * @param id                 the id
     * @return the string
     */
    @PutMapping("/mrd-pool/{id}")
    public String update(@RequestBody MediaRoutingDomain mediaRoutingDomain, @PathVariable String id) {
        if (mrdPool.contains(id)) {
            mediaRoutingDomain.setId(id);
            this.mrdPool.update(mediaRoutingDomain);
            return "Success";
        }
        return "Could not find MRD to update";
    }

    /**
     * Retrieve response entity.
     *
     * @return the response entity
     */
    @GetMapping("/mrd-pool")
    public ResponseEntity<Object> retrieve() {
        return new ResponseEntity<>(this.mrdPool.findAll(), HttpStatus.OK);
    }
}
