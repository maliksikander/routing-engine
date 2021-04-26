package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.CommonEnums;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatchSelectedAgent implements PropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchSelectedAgent.class);

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.CommandProperties.DISPATCH_SELECTED_AGENT.toString())) {
            LOGGER.info("Received event: {}", evt.getPropertyName());
        }
    }
}
