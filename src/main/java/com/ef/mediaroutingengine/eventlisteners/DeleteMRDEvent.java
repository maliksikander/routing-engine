package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.CommonEnums;
import com.ef.mediaroutingengine.repositories.MrdPool;
import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeleteMRDEvent implements PropertyChangeListener {
    private static final Logger LOGGER = LogManager.getLogger(DeleteMRDEvent.class.getName());

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.EventProperties.DELETE_MRD.toString())) {
                LOGGER.debug("DeleteMRDEvent() begin");
                JsonNode node = (JsonNode) evt.getNewValue();
                String mrdName = node.get("Name").textValue();
                MrdPool.getInstance().removeMRD(mrdName);
                LOGGER.debug("DeleteMRDEvent() end");
            }
        } catch (Exception ex) {
            LOGGER.warn("Exception in DeleteMRDEvent(), ex.Message: {}", ex.getMessage());
        }
    }
}
