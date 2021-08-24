package com.ef.mediaroutingengine.services.pools;

import com.ef.cim.objectmodel.RoutingAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Routing attributes pool.
 */
@Service
public class RoutingAttributesPool {
    /**
     * The Pool.
     */
    private final Map<UUID, RoutingAttribute> pool;

    /**
     * Instantiates a new Routing attributes pool.
     */
    @Autowired
    public RoutingAttributesPool() {
        this.pool = new ConcurrentHashMap<>();
    }

    /**
     * Load from.
     *
     * @param routingAttributes the routing attributes
     */
    public void loadFrom(List<RoutingAttribute> routingAttributes) {
        routingAttributes.forEach(routingAttribute -> pool.put(routingAttribute.getId(), routingAttribute));
    }

    /**
     * Find by id routing attribute.
     *
     * @param id the id
     * @return the routing attribute
     */
    public RoutingAttribute findById(UUID id) {
        if (id == null) {
            return null;
        }
        return this.pool.get(id);
    }

    /**
     * Find all list.
     *
     * @return the list
     */
    public List<RoutingAttribute> findAll() {
        List<RoutingAttribute> result = new ArrayList<>();
        this.pool.forEach((k, v) -> result.add(v));
        return result;
    }

    /**
     * Contains boolean.
     *
     * @param id the id
     * @return the boolean
     */
    public boolean contains(UUID id) {
        if (id == null) {
            return false;
        }
        return this.pool.containsKey(id);
    }

    /**
     * Insert boolean.
     *
     * @param routingAttribute the routing attribute
     * @return the boolean
     */
    public boolean insert(RoutingAttribute routingAttribute) {
        return this.pool.putIfAbsent(routingAttribute.getId(), routingAttribute) == null;
    }

    /**
     * Update boolean.
     *
     * @param routingAttribute the routing attribute
     * @return the boolean
     */
    public boolean update(RoutingAttribute routingAttribute) {
        RoutingAttribute existing = this.pool.get(routingAttribute.getId());
        if (existing != null) {
            existing.setName(routingAttribute.getName());
            existing.setDescription(routingAttribute.getDescription());
            existing.setType(routingAttribute.getType());
            existing.setDefaultValue(routingAttribute.getDefaultValue());
            return true;
        }
        return false;
    }

    /**
     * Save.
     *
     * @param routingAttribute the routing attribute
     */
    public void save(RoutingAttribute routingAttribute) {
        if (routingAttribute.getId() == null) {
            return;
        }
        boolean updated = this.update(routingAttribute);
        if (!updated) {
            this.insert(routingAttribute);
        }
    }

    /**
     * Delete by id.
     *
     * @param id the id
     */
    public void deleteById(UUID id) {
        if (id == null) {
            return;
        }
        this.pool.remove(id);
    }
}
