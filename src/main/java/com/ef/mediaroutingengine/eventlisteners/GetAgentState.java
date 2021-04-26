package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.CommonEnums;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetAgentState implements PropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetAgentState.class);

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.CommandProperties.GET_AGENT_STATE.name())) {
            Agent agent = (Agent) evt.getSource();
            LOGGER.debug("Handle command begin for agent: {}, command: {}",
                    agent.getId(), CommonEnums.CommandProperties.GET_AGENT_STATE);
            LOGGER.info("evt.getPropertyName() = {}", evt.getPropertyName());
            LOGGER.debug("Handle command end");
        }
    }

}
