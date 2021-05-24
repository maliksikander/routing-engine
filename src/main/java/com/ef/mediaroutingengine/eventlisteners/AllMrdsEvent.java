package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.services.MrdPool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AllMrdsEvent implements PropertyChangeListener {
    private static final Logger LOGGER = LogManager.getLogger(AllMrdsEvent.class.getName());

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.ALL_MRDS.name())) {
                LOGGER.debug("AddMrdsEvent() begin");
                ArrayNode newValue = (ArrayNode) evt.getNewValue();
                for (final JsonNode node : newValue) {
                    String mrdName = node.get("Name").textValue();
                    String mrdDescription = node.get("Description").textValue();
                    boolean interruptible = Boolean.valueOf(node.get("Interruptible").textValue());

                    MediaRoutingDomain mrd = new MediaRoutingDomain();
                    mrd.setName(mrdName);
                    mrd.setDescription(mrdDescription);
                    mrd.setInterruptible(interruptible);
                    // No locking required here, locked while adding mrd in MrdPool
                    MrdPool.getInstance().addMrd(mrd);
                }
                LOGGER.debug("AddMrdsEvent() end");
            }
        } catch (Exception ex) {
            LOGGER.info("Exception in AddMrdsEvent(), Message: {}", ex.getMessage());
        }
    }
}
