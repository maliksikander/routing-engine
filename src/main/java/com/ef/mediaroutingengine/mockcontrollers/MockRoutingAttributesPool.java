package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.RoutingAttributesPool;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Mock routing attributes pool.
 */
@RestController
public class MockRoutingAttributesPool {
    /**
     * The Routing attributes pool.
     */
    private final RoutingAttributesPool routingAttributesPool;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;

    /**
     * Instantiates a new Mock routing attributes pool.
     *
     * @param routingAttributesPool the routing attributes pool
     * @param agentsPool            the agents pool
     * @param precisionQueuesPool   the precision queues pool
     */
    @Autowired
    public MockRoutingAttributesPool(RoutingAttributesPool routingAttributesPool, AgentsPool agentsPool,
                                     PrecisionQueuesPool precisionQueuesPool) {
        this.routingAttributesPool = routingAttributesPool;
        this.agentsPool = agentsPool;
        this.precisionQueuesPool = precisionQueuesPool;
    }

    /**
     * Update attribute string.
     *
     * @param routingAttribute the routing attribute
     * @param id               the id
     * @return the string
     */
    @PutMapping("/routing-attributes-pool/{id}")
    public String updateAttribute(@RequestBody RoutingAttribute routingAttribute, @PathVariable UUID id) {
        if (this.routingAttributesPool.contains(id)) {
            routingAttribute.setId(id);
            this.routingAttributesPool.save(routingAttribute);
            return "Success";
        }
        return "Id not found";
    }

    /**
     * Retrieve response entity.
     *
     * @return the response entity
     */
    @GetMapping("/routing-attributes-pool")
    public ResponseEntity<Object> retrieve() {
        return new ResponseEntity<>(this.routingAttributesPool.findAll(), HttpStatus.OK);
    }

    /**
     * Retrieve agents response entity.
     *
     * @return the response entity
     */
    @GetMapping("/agents-pool")
    public ResponseEntity<Object> retrieveAgents() {
        return new ResponseEntity<>(this.agentsPool.findAll(), HttpStatus.OK);
    }

    /**
     * Retrieve queues response entity.
     *
     * @return the response entity
     */
    @GetMapping("/precision-queue-pool")
    public ResponseEntity<Object> retrieveQueues() {
        return new ResponseEntity<>(this.precisionQueuesPool.toList(), HttpStatus.OK);
    }
}
