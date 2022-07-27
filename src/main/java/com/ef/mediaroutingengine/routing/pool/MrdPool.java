package com.ef.mediaroutingengine.routing.pool;

import com.ef.cim.objectmodel.MediaRoutingDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * The type Mrd pool.
 */
@Service
public class MrdPool {
    /**
     * The MRD pool.
     */
    private final Map<String, MediaRoutingDomain> pool = new ConcurrentHashMap<>();

    /**
     * Load pool at start of application.
     *
     * @param mediaRoutingDomains list of media-routing-domains from config db.
     */
    public void loadPoolFrom(List<MediaRoutingDomain> mediaRoutingDomains) {
        for (MediaRoutingDomain mediaRoutingDomain : mediaRoutingDomains) {
            this.pool.put(mediaRoutingDomain.getId(), mediaRoutingDomain);
        }
    }

    /**
     * Contains boolean.
     *
     * @param id the id
     * @return the boolean
     */
    public boolean contains(String id) {
        if (id == null) {
            return false;
        }
        return this.pool.containsKey(id);
    }

    /**
     * Insert.
     *
     * @param mediaRoutingDomain the media routing domain
     */
    public void insert(MediaRoutingDomain mediaRoutingDomain) {
        this.pool.putIfAbsent(mediaRoutingDomain.getId(), mediaRoutingDomain);
    }

    /**
     * Update.
     *
     * @param mediaRoutingDomain the media routing domain
     */
    public void update(MediaRoutingDomain mediaRoutingDomain) {
        MediaRoutingDomain existing = this.pool.get(mediaRoutingDomain.getId());
        if (existing != null) {
            existing.setName(mediaRoutingDomain.getName());
            existing.setDescription(mediaRoutingDomain.getDescription());
            existing.setInterruptible(mediaRoutingDomain.isInterruptible());
            existing.setMaxRequests(mediaRoutingDomain.getMaxRequests());
        }
    }

    /**
     * Searches MRD by name in the pool and returns if found.
     *
     * @param id id of the MRD to return.
     * @return MRD if found, null otherwise
     */
    public MediaRoutingDomain findById(String id) {
        if (id == null) {
            return null;
        }
        return this.pool.get(id);
    }

    /**
     * Returns all the MRDs in the pool.
     *
     * @return List of all MRDs.
     */
    public List<MediaRoutingDomain> findAll() {
        List<MediaRoutingDomain> mediaRoutingDomains = new ArrayList<>();
        this.pool.forEach((k, v) -> mediaRoutingDomains.add(v));
        return mediaRoutingDomains;
    }

    /**
     * Delete by id.
     *
     * @param id the id
     */
    public void deleteById(String id) {
        if (id != null) {
            this.pool.remove(id);
        }
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return this.pool.size();
    }
}
