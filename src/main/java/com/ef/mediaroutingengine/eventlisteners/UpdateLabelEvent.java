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

public class UpdateLabelEvent implements PropertyChangeListener {
    private static final Logger log = LogManager.getLogger(UpdateLabelEvent.class.getName());

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.EventProperties.UPDATE_LABEL.name())) {
                log.debug("UpdateLabelEvent onEvent() Started");
                String newValue = (String) evt.getNewValue();
                ObjectMapper objectMapper = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                JavaType type = objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, PriorityLabel.class);
                List<PriorityLabel> labels = objectMapper.readValue(newValue, type);
                if (labels.size() == 0) {
                    return;
                }
                PriorityLabelsPool.getInstance().updatePriorityLabel(labels.get(0));
                log.debug("UpdateLabelEvent onEvent() Ended");
            }
        } catch (Exception ex) {
            log.error("Exception in UpdateLabelEvent(), message: {}", ExceptionUtils.getMessage(ex));
            log.error("Stack Trace: {}", ExceptionUtils.getStackTrace(ex));
        }
    }
}
