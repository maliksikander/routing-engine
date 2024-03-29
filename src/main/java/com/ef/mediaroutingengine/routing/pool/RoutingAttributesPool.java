package com.ef.mediaroutingengine.routing.pool;

import com.ef.cim.objectmodel.RoutingAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * The type Routing attributes pool.
 */
@Service
public class RoutingAttributesPool {
    /**
     * The Pool.
     */
    private final Map<String, RoutingAttribute> pool = new ConcurrentHashMap<>();

    /**
     * Load from.
     *
     * @param routingAttributes the routing attributes
     */
    public void loadFrom(List<RoutingAttribute> routingAttributes) {
        this.pool.clear();
        routingAttributes.forEach(routingAttribute -> pool.put(routingAttribute.getId(), routingAttribute));
    }

    /**
     * Find by id routing attribute.
     *
     * @param id the id
     * @return the routing attribute
     */
    public RoutingAttribute findById(String id) {
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
    public void deleteById(String id) {
        if (id == null) {
            return;
        }
        this.pool.remove(id);
    }

    public int size() {
        return this.pool.size();
    }
}
