package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.PriorityLabel;
import com.ef.mediaroutingengine.services.PriorityLabelsPool;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AllLabelsEvent implements PropertyChangeListener {
    private static Logger log = LogManager.getLogger(AllLabelsEvent.class.getName());

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.ALL_LABELS.name())) {
                log.debug("AllLabelsEvent onEvent() Started");
                String newValue = (String) evt.getNewValue();
                ObjectMapper objectMapper = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                JavaType type = objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, PriorityLabel.class);
                PriorityLabelsPool.getInstance()
                        .addPriorityLabel((List<PriorityLabel>) objectMapper.readValue(newValue, type));
                log.debug("AllLabelsEvent onEvent() Ended");
            }
        } catch (Exception ex) {
            log.error("Exception in AllLabelsEvent(), message: {}", ExceptionUtils.getMessage(ex));
            log.error("Stack Trace: {}", ExceptionUtils.getStackTrace(ex));
            log.debug("AllLabelsEvent onEvent() Ended");
        }
    }
}