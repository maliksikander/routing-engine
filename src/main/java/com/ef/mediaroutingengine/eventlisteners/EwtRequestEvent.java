package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.CommonEnums;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EwtRequestEvent implements PropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EwtRequestEvent.class);

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.EventProperties.EWT_REQUEST_EVENT.toString())) {
            LOGGER.info("EWT_REQUEST_EVENT property listener");
        }
    }
}
