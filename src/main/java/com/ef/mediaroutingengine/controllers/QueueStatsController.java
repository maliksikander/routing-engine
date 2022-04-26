package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.services.controllerservices.QueueStatsService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Queue stats controller.
 */
@RestController
public class QueueStatsController {
    /**
     * The Service.
     */
    private final QueueStatsService service;

    /**
     * Instantiates a new Queue stats controller.
     *
     * @param service the service
     */
    @Autowired
    public QueueStatsController(QueueStatsService service) {
        this.service = service;
    }

    /**
     * Gets queue stats.
     *
     * @param queueId the queue id
     * @return the queue stats
     */
    @GetMapping("/queue-stats")
    public ResponseEntity<Object> getQueueStats(@RequestParam Optional<String> queueId) {
        return queueId.<ResponseEntity<Object>>map(s -> ResponseEntity.ok(this.service.getQueueStats(s)))
                .orElseGet(() -> ResponseEntity.ok(this.service.getQueueStatsForAll()));
    }
}
