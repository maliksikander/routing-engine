package com.ef.mediaroutingengine.services.pools;

import com.ef.mediaroutingengine.eventlisteners.AllMrdsEvent;
import com.ef.mediaroutingengine.eventlisteners.DeleteMRDEvent;
import com.ef.mediaroutingengine.eventlisteners.NewMRDEvent;
import com.ef.mediaroutingengine.eventlisteners.UpdateMRDEvent;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MrdPool {
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private static MrdPool instance;
    private final List<PropertyChangeListener> listeners = new LinkedList<>();

    private final List<MediaRoutingDomain> mrds = new LinkedList<>();

    private MrdPool() {
        initialize();
    }

    /**
     * Get the singleton instance of MrdPool.
     *
     * @return the singleton instance
     */
    public static MrdPool getInstance() {
        if (instance == null) {
            instance = new MrdPool();
        }
        return instance;
    }

    private void initialize() {
        this.listeners.add(new NewMRDEvent());
        this.listeners.add(new UpdateMRDEvent());
        this.listeners.add(new DeleteMRDEvent());
        this.listeners.add(new AllMrdsEvent());

        for (PropertyChangeListener listener : this.listeners) {
            this.changeSupport.addPropertyChangeListener(listener);
        }
    }

    /**
     * Adds a MRD in the pool.
     *
     * @param mrd object to add
     * @return true if added successfully, false otherwise
     */
    public boolean addMrd(MediaRoutingDomain mrd) {
        boolean result = false;
        synchronized (mrds) {
            result = this.mrds.add(mrd);
        }
        return result;
    }

    /**
     * Updates an existing MRD in the pool.
     *
     * @param mrd the updated object
     * @return true if found and updated. false otherwise
     */
    public boolean updateMrd(MediaRoutingDomain mrd) {
        boolean result = false;
        for (MediaRoutingDomain element : mrds) {
            if (element.getName().equalsIgnoreCase(mrd.getName())) {
                synchronized (mrds) {
                    element.setDescription(mrd.getDescription());
                    element.setInterruptible(mrd.isInterruptible());
                }
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Searches MRD by name in the pool and returns if found.
     *
     * @param name name of the MRD to return.
     * @return MRD if found, null otherwise
     */
    public MediaRoutingDomain getMrd(String name) {
        MediaRoutingDomain result = null;
        for (MediaRoutingDomain mrd : mrds) {
            if (mrd.getName().equalsIgnoreCase(name)) {
                result = mrd;
                break;
            }
        }
        return result;
    }

    /**
     * Finds and returns the MRD Object in the pool.
     *
     * @param mrd the object to find.
     * @return MRD object if found, null otherwise.
     */
    public MediaRoutingDomain getMrd(MediaRoutingDomain mrd) {
        return getMrd(mrd.getName());
    }

    /**
     * Removes a MRD object by name in the pool.
     *
     * @param name name of the MRD to remove
     * @return true if found and deleted, false otherwise
     */
    public boolean removeMRD(String name) {
        boolean result = false;
        for (MediaRoutingDomain mrd : mrds) {
            if (mrd.getName().equalsIgnoreCase(name)) {
                synchronized (mrds) {
                    this.mrds.remove(mrd);
                }
                break;
            }
        }
        return result;
    }

    public List<MediaRoutingDomain> getAllMrds() {
        return this.mrds;
    }

    public void handleAllMrdsEvent(Enums.EventName property, JsonNode node) {
        this.changeSupport.firePropertyChange(property.name(), null, node);
    }

    public void handleNewMrdEvent(Enums.EventName property, JsonNode node) {
        this.changeSupport.firePropertyChange(property.name(), null, node);
    }

    public void handleUpdateMrdEvent(Enums.EventName property, JsonNode node) {
        this.changeSupport.firePropertyChange(property.name(), null, node);
    }

    public void handleDeleteMrdEvent(Enums.EventName property, JsonNode node) {
        this.changeSupport.firePropertyChange(property.name(), null, node);
    }
}
