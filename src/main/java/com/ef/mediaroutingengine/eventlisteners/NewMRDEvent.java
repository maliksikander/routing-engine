package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NewMRDEvent implements PropertyChangeListener {
    private static final Logger LOGGER = LogManager.getLogger(NewMRDEvent.class.getName());

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.NEW_MRD.toString())) {
                LOGGER.debug("NewMRDEvent() begin");
                JsonNode node = (JsonNode) evt.getNewValue();
                String mrdName = node.get("Name").textValue();
                if (MrdPool.getInstance().getMrd(mrdName) != null) {
                    LOGGER.warn("Mrd with same name already exists");
                    LOGGER.debug("NewMrdEvent() end");
                    return;
                }
                String mrdDescription = node.get("Description").textValue();
                boolean interruptible = Boolean.valueOf(node.get("Interruptible").textValue());

                MediaRoutingDomain mrd = new MediaRoutingDomain();
                mrd.setName(mrdName);
                mrd.setDescription(mrdDescription);
                mrd.setInterruptible(interruptible);
                // No locking required here, locked while adding mrd in MrdPool
                MrdPool.getInstance().addMrd(mrd);
                LOGGER.debug("NewMRDEvent() end");
            }
        } catch (Exception ex) {
            LOGGER.warn("Exception in NewMRDEvent, ex.Message: {}", ex.getMessage());
        }

    }
}
