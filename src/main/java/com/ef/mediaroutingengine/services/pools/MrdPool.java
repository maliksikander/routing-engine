package com.ef.mediaroutingengine.services.pools;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class MrdPool {
    private final Map<UUID, MediaRoutingDomain> mrds = new ConcurrentHashMap<>();

    /**
     * Load pool at start of application.
     *
     * @param mediaRoutingDomains list of media-routing-domains from config db.
     */
    public void loadPoolFrom(List<MediaRoutingDomain> mediaRoutingDomains) {
        for (MediaRoutingDomain mediaRoutingDomain : mediaRoutingDomains) {
            this.mrds.put(mediaRoutingDomain.getId(), mediaRoutingDomain);
        }
    }

    /**
     * Searches MRD by name in the pool and returns if found.
     *
     * @param id id of the MRD to return.
     * @return MRD if found, null otherwise
     */
    public MediaRoutingDomain findById(UUID id) {
        return this.mrds.get(id);
    }

    /**
     * Returns all the MRDs in the pool.
     *
     * @return List of all MRDs.
     */
    public List<MediaRoutingDomain> findAll() {
        List<MediaRoutingDomain> mediaRoutingDomains = new ArrayList<>();
        this.mrds.forEach((k, v) -> mediaRoutingDomains.add(v));
        return mediaRoutingDomains;
    }

    public int size() {
        return this.mrds.size();
    }
}
