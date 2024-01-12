package com.ef.mediaroutingengine.routing.pool;

import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.MrdType;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final Map<String, MrdType> mrdTypes = new HashMap<>();

    /**
     * Load pool at start of application.
     *
     * @param mediaRoutingDomains list of media-routing-domains from config db.
     */
    public void loadFrom(List<MrdType> mrdTypes, List<MediaRoutingDomain> mediaRoutingDomains) {
        this.mrdTypes.clear();
        mrdTypes.forEach((t -> this.mrdTypes.put(t.getId(), t)));

        this.pool.clear();
        mediaRoutingDomains.forEach(mrd -> this.pool.put(mrd.getId(), mrd));
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
            existing.setType(mediaRoutingDomain.getType());
            existing.setDescription(mediaRoutingDomain.getDescription());
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

    /**
     * Gets type.
     *
     * @param mrdId the mrd id
     * @return the type
     */
    public MrdType getType(String mrdId) {
        MediaRoutingDomain mrd = this.findById(mrdId);

        if (mrd == null) {
            return null;
        }

        return this.mrdTypes.get(mrd.getType());
    }
}
