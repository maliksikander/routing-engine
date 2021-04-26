package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.CommonEnums;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewTaskEvent implements PropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewTaskEvent.class);

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.EventProperties.NEW_TASK.toString())) {
            LOGGER.info("Job name: {}, new Value: {}", evt.getPropertyName(), evt.getNewValue());
        }
    }
}
