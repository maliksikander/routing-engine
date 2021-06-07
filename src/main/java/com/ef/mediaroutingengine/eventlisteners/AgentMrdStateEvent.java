package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.dto.AgentMrdStateChangedRequest;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Enums;
import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class AgentMrdStateEvent implements PropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentMrdStateEvent.class);
    /*
     this.changeSupport => fires property change to Task Schedulers
     */
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private final List<String> precisionQueueChangeSupportListeners = new LinkedList<>();


    /**
     * Adds a property change listener to the changeSupport.
     *
     * @param listener the property change listener object
     * @param name     the name of the listener to avoid duplicates
     */
    public void addPropertyChangeListener(PropertyChangeListener listener, String name) {
        if (!this.precisionQueueChangeSupportListeners.contains(name)) {
            this.precisionQueueChangeSupportListeners.add(name);
            this.changeSupport.addPropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener, String name) {
        this.precisionQueueChangeSupportListeners.remove(name);
        this.changeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (agentMrdStateChanged(evt)) {
            AgentMrdStateChangedRequest request = (AgentMrdStateChangedRequest) evt.getNewValue();
            Agent agent = (Agent) evt.getSource();
            agent.setMrdStates(request.getAgentMrdStates());
            // TODO: CHECK if AGENT MODE CAN BE CHANGED FROM AGENT MANAGER
            // agent.setAgentMode(this.getAgentModeFrom(eventNewValue));

            this.fireStateChangeToTaskSchedulersIfStateActiveOrReady(agent.getState());
            LOGGER.debug("Agent State Listener for agent: {}", agent.getId());
        }
    }

    private boolean agentMrdStateChanged(PropertyChangeEvent evt) {
        return evt.getPropertyName().equalsIgnoreCase(Enums.EventName.AGENT_MRD_STATE.name());
    }

    private Enums.AgentStateName getAgentStateFrom(JsonNode newValue) {
        String newStateString = newValue.get("State").textValue();
        return Enums.AgentStateName.valueOf(newStateString);
    }

    private Enums.AgentMode getAgentModeFrom(JsonNode newValue) {
        boolean isRoutable = newValue.get("Routable").toString().equalsIgnoreCase("true");
        return isRoutable ? Enums.AgentMode.ROUTABLE : Enums.AgentMode.NON_ROUTABLE;
    }

    private void fireStateChangeToTaskSchedulersIfStateActiveOrReady(Enums.AgentStateName state) {
        switch (state) {
            case READY:
            case ACTIVE:
                this.changeSupport.firePropertyChange("StateChange", null, state);
                break;
            default:
                break;
        }
    }

}
