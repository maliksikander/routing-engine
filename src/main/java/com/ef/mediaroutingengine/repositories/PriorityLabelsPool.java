package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.eventlisteners.AllLabelsEvent;
import com.ef.mediaroutingengine.eventlisteners.DeleteLabelEvent;
import com.ef.mediaroutingengine.eventlisteners.NewLabelEvent;
import com.ef.mediaroutingengine.eventlisteners.UpdateLabelEvent;
import com.ef.mediaroutingengine.model.CommonEnums;
import com.ef.mediaroutingengine.model.PriorityLabel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriorityLabelsPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(PriorityLabelsPool.class);

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private final List<PropertyChangeListener> listeners = new LinkedList<>();
    private static PriorityLabelsPool _instance;
    private final Map<String, PriorityLabel> labels;

    private PriorityLabelsPool() {
        this.labels = Collections.synchronizedMap(new LinkedHashMap<>());
        initialize();
    }

    /**
     * Returns the singleton instance of the class.
     *
     * @return the singleton instance.
     */
    public static PriorityLabelsPool getInstance() {
        if (_instance == null) {
            synchronized (PriorityLabelsPool.class) {
                if (_instance == null) {
                    _instance = new PriorityLabelsPool();
                    LOGGER.debug("Created PriorityLabelsPool instance");
                }
            }
        }
        return _instance;
    }

    private void initialize() {
        this.listeners.add(new NewLabelEvent());
        this.listeners.add(new UpdateLabelEvent());
        this.listeners.add(new DeleteLabelEvent());
        this.listeners.add(new AllLabelsEvent());

        for (PropertyChangeListener listener : this.listeners) {
            this.changeSupport.addPropertyChangeListener(listener);
        }
    }

    public Map<String, PriorityLabel> getLabels() {
        return labels;
    }

    public PriorityLabel getPriorityLabel(String name) {
        return this.labels.getOrDefault(name, null);
    }

    /**
     * Adds a priority label to the pool.
     *
     * @param label the priority label to be added
     */
    public void addPriorityLabel(PriorityLabel label) {
        LOGGER.info("Going to add priority label in pool, name: {}, priority: {}",
                label.getName(), label.getPriority());
        // LinkedMap keys are case sensitive, cannot use TreeMap with case insensitive keys because
        // we need to keep insertion order
        this.labels.put(label.getName().toUpperCase(), label);
        LOGGER.info("Updated priority labels pool {}", this.labels);
    }

    /**
     * Adds a priority label list to the pool.
     *
     * @param labelsList the priority label list to be added
     */
    public void addPriorityLabel(List<PriorityLabel> labelsList) {
        LOGGER.info("Going to add priority labels list in pool, {}", labelsList);
        labelsList.forEach(x -> this.labels.put(x.getName().toUpperCase(), x));
        LOGGER.info("Updated priority labels pool {}", this.labels);
    }

    /**
     * Removes a priority label from the pool.
     *
     * @param label the priority label to be removed
     */
    public void removePriorityLabel(PriorityLabel label) {
        LOGGER.info("Going to remove priority label in pool, name: {}, priority: {}",
                label.getName(), label.getPriority());
        this.labels.remove(label.getName().toUpperCase());
        LOGGER.info("Updated priority labels pool {}", this.labels);
    }

    /**
     * Removes the priority label by name from the pool.
     *
     * @param name the name of the label to be removed
     */
    public void removePriorityLabel(String name) {
        PriorityLabel label = this.labels.getOrDefault(name, null);
        if (label != null) {
            removePriorityLabel(label);
        }
    }

    /**
     * Updates an existing priority label in the pool.
     *
     * @param label the priority label to be updated
     */
    public void updatePriorityLabel(PriorityLabel label) {
        LOGGER.info("Going to update priority label in pool, name: {}, priority: {}",
                label.getName(), label.getPriority());
        this.labels.put(label.getName().toUpperCase(), label);
        LOGGER.info("Updated priority labels pool {}", this.labels);
    }

    /**
     * Filters and returns the pool.
     *
     * @param labelsToFilter the labels name to be included in the filtered pool.
     * @return the filtered priority label pool
     * @throws RuntimeException if there are duplicate label names in the argument list.
     */
    public Map<String, PriorityLabel> filterLabels(List<String> labelsToFilter) throws RuntimeException {
        return this.labels.entrySet().stream()
                .filter(x -> labelsToFilter.stream().anyMatch(x.getKey()::equalsIgnoreCase))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (v1, v2) -> {
                            throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        LinkedHashMap::new));
    }

    public void handleLabelEvents(CommonEnums.EventProperties property, String messageBody) {
        this.changeSupport.firePropertyChange(property.name(), "", messageBody);
    }

    public boolean contains(String label) {
        return this.labels.containsKey(label);
    }

    public int getPriority(String label) {
        PriorityLabel priorityLabel = this.labels.get(label);
        return priorityLabel != null ? priorityLabel.getPriority() : -1;
    }
}
