package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateMRDEvent implements PropertyChangeListener {
    private static final Logger LOGGER = LogManager.getLogger(UpdateMRDEvent.class.getName());

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.UPDATE_MRD.toString())) {
                LOGGER.debug("UpdateMRDEvent() begin");
                JsonNode node = (JsonNode) evt.getNewValue();
                String mrdName = node.get("Name").textValue();
                String mrdDescription = node.get("Description").textValue();
                boolean interruptible = Boolean.valueOf(node.get("Interruptible").textValue());

                MediaRoutingDomain mrd = new MediaRoutingDomain();
                mrd.setName(mrdName);
                mrd.setDescription(mrdDescription);
                mrd.setInterruptible(interruptible);
                // No locking required here, locked while updating mrd in MrdPool
                MrdPool.getInstance().updateMrd(mrd);
                LOGGER.debug("UpdateMRDEvent() end");
            }
        } catch (Exception ex) {
            LOGGER.warn("Exception in UpdateMRDEvent(), ex.Message: {}", ex.getMessage());
        }
    }
}
