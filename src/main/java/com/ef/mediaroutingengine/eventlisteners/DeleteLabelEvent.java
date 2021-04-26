package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.CommonEnums;
import com.ef.mediaroutingengine.model.PriorityLabel;
import com.ef.mediaroutingengine.repositories.PriorityLabelsPool;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeleteLabelEvent implements PropertyChangeListener {
    private static final Logger LOGGER = LogManager.getLogger(DeleteLabelEvent.class.getName());

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.EventProperties.DELETE_LABEL.name())) {
                LOGGER.debug("DeleteLabelEvent onEvent() Started");
                String newValue = (String) evt.getNewValue();
                ObjectMapper objectMapper = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, PriorityLabel.class);
                List<PriorityLabel> labels = objectMapper.readValue(newValue, type);
                if (labels.size() == 0) {
                    return;
                }
                PriorityLabelsPool.getInstance().removePriorityLabel(labels.get(0));
                LOGGER.debug("DeleteLabelEvent onEvent() Ended");
            }
        } catch (Exception ex) {
            LOGGER.error("Exception in DeleteLabelEvent(), message: {}", ExceptionUtils.getMessage(ex));
            LOGGER.error("Stack Trace: {}", ExceptionUtils.getStackTrace(ex));
        }
    }
}
