package com.ef.mediaroutingengine.routing.pool;

import com.ef.cim.objectmodel.MrdType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * The type Mrd type pool.
 */
@Service
public class MrdTypePool {
    /**
     * The Pool.
     */
    private final Map<String, MrdType> pool = new ConcurrentHashMap<>();

    /**
     * Load from.
     *
     * @param mrdTypeList the mrd type list
     */
    public void loadFrom(List<MrdType> mrdTypeList) {
        this.pool.clear();
        for (MrdType mrdType : mrdTypeList) {
            this.pool.put(mrdType.getId(), mrdType);
        }
    }

    /**
     * Gets by id.
     *
     * @param id the id
     * @return the by id
     */
    public MrdType getById(String id) {
        return this.pool.get(id);
    }
}
