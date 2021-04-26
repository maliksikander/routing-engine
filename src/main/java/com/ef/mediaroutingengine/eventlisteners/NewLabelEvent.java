package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.CommonEnums;
import com.ef.mediaroutingengine.model.PriorityLabel;
import com.ef.mediaroutingengine.repositories.PriorityLabelsPool;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NewLabelEvent implements PropertyChangeListener {
    private static Logger log = LogManager.getLogger(NewLabelEvent.class.getName());

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.EventProperties.NEW_LABEL.name())) {
                log.debug("NewLabelEvent onEvent() Started");
                String newValue = (String) evt.getNewValue();
                ObjectMapper objectMapper = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                PriorityLabelsPool.getInstance()
                        .addPriorityLabel(objectMapper.readValue(newValue, PriorityLabel.class));
                log.debug("NewLabelEvent onEvent() Ended");
            }
        } catch (Exception ex) {
            log.error("Exception in NewLabelEvent(), message: {}", ExceptionUtils.getMessage(ex));
            log.error("Stack Trace: {}", ExceptionUtils.getStackTrace(ex));
        }
    }
}
