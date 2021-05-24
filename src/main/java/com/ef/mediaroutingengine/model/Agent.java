package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangedRequest;
import com.ef.mediaroutingengine.eventlisteners.AgentMrdStateEvent;
import com.ef.mediaroutingengine.eventlisteners.GetAgentState;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agent {
    private static final Logger LOGGER = LoggerFactory.getLogger(Agent.class);

    private final KeycloakUser keycloakUser;
    private final List<AssociatedRoutingAttribute> associatedRoutingAttributes;

    private Enums.AgentState agentState;
    private List<AgentMrdState> agentMrdStates;
    private Enums.AgentMode agentMode = Enums.AgentMode.NON_ROUTABLE;
    private AtomicInteger numOfTasks;
    private LocalDateTime lastReadyStateChangeTime;
    // Todo: Make assigned task a synchronized list
    private final List<Task> assignedTasks;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private List<PropertyChangeListener> listeners;

    /**
     * Default constructor, An Agent object can only be created from a CCUser object.
     *
     * @param ccUser A CCUser object to create the instance from.
     */
    public Agent(@NotNull CCUser ccUser) {
        this.keycloakUser = ccUser.getKeycloakUser();
        this.associatedRoutingAttributes = ccUser.getAssociatedRoutingAttributes();

        this.numOfTasks = new AtomicInteger(0);
        this.lastReadyStateChangeTime = LocalDateTime.of(1990, 4, 2, 12, 1);
        this.assignedTasks = Collections.synchronizedList(new ArrayList<>());

        this.initialize();
    }

    private void initialize() {
        this.listeners = new LinkedList<>();
        this.listeners.add(new GetAgentState());
        this.listeners.add(new AgentMrdStateEvent());

        for (PropertyChangeListener listener : this.listeners) {
            this.changeSupport.addPropertyChangeListener(listener);
        }
    }

    /**
     * Adds a schedule listener to AgentStateEvent objects' property change support.
     *
     * @param listener the property change listener to be added
     * @param name the name of the listener
     */
    public void addSchedulerListener(PropertyChangeListener listener, String name) {
        for (PropertyChangeListener i : this.listeners) {
            if (i instanceof AgentMrdStateEvent) {
                ((AgentMrdStateEvent) i).addPropertyChangeListener(listener, name);
            }
        }
    }

    public boolean taskExists(Task task) {
        return this.assignedTasks.contains(task);
    }

    /**
     * Assigns a task to the current Agent's object.
     *
     * @param task the task to be added.
     */
    public void assignTask(Task task) {
        if (task == null) {
            LOGGER.debug("Cannot assign task, taskService is null");
            return;
        }
        if (!taskExists(task)) {
            this.assignedTasks.add(task);
        }
        LOGGER.debug("Agent Id: {}. Task: {} assigned. Total tasks handling: {}.",
                this.keycloakUser.getId(), task.getId(), this.assignedTasks.size());
    }

    public void changeMrdState(AgentMrdStateChangedRequest request) {
        this.changeSupport.firePropertyChange(Enums.EventName.AGENT_MRD_STATE.name(), null, request);
    }

    /**
     * Sets the Mrd states of the Agent.
     *
     * @param updated the list of updated Mrd states
     */
    public void setMrdStates(List<AgentMrdState> updated) {
        for (AgentMrdState newMrdState: updated) {
            for (AgentMrdState oldMrdState: this.agentMrdStates) {
                if (oldMrdState.getMrdId().equals(newMrdState.getMrdId())) {
                    oldMrdState.setState(newMrdState.getState());
                    break;
                }
            }
        }
    }

    /**
     * Ends task assigned to the current Agent's object.
     *
     * @param task the task to end.
     */
    public void endTask(Task task) {
        if (taskExists(task)) {
            if (task.getStartTime() != null) {
                task.setHandlingTime(System.currentTimeMillis() - task.getStartTime());
            } else {
                task.setHandlingTime(0L);
            }
            task.setStartTime(System.currentTimeMillis());
            this.assignedTasks.remove(task);
        }
        LOGGER.debug("Agent Id: {}. Task : {} removed. Total tasks handling: {}",
                this.keycloakUser.getId(), task.getId(), this.assignedTasks.size());
    }

    public UUID getId() {
        return this.keycloakUser.getId();
    }

    public List<AssociatedRoutingAttribute> getAssociatedRoutingAttributes() {
        return this.associatedRoutingAttributes;
    }

    public Enums.AgentState getState() {
        return this.agentState;
    }

    /**
     * Sets the Agent's state.
     *
     * @param state the state to be set
     */
    public void setState(Enums.AgentState state) {
        this.agentState = state;
        if (state == Enums.AgentState.LOGOUT) {
            synchronized (this.assignedTasks) {
                this.assignedTasks.clear();
            }
        }
        if (state == Enums.AgentState.READY) {
            this.setReadyStateChangeTime(LocalDateTime.now());
        }
    }

    public List<AgentMrdState> getAgentMrdStates() {
        return agentMrdStates;
    }

    public void setAgentMrdStates(List<AgentMrdState> agentMrdStates) {
        this.agentMrdStates = agentMrdStates;
    }

    public Enums.AgentMode getAgentMode() {
        return agentMode;
    }

    public void setAgentMode(Enums.AgentMode agentMode) {
        this.agentMode = agentMode;
    }

    public int getNumOfTasks() {
        return this.assignedTasks.size();
    }

    public void setReadyStateChangeTime(LocalDateTime time) {
        this.lastReadyStateChangeTime = time;
    }

    /**
     * Converts the Agent object to CCUser object.
     *
     * @return the converted CCUser object
     */
    public CCUser toCcUser() {
        CCUser ccUser = new CCUser();
        ccUser.setId(this.getId());
        ccUser.setKeycloakUser(this.keycloakUser);
        ccUser.setAssociatedRoutingAttributes(this.associatedRoutingAttributes);
        return ccUser;
    }
}
