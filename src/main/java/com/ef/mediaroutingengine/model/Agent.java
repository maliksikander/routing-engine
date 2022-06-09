package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.mediaroutingengine.commons.Enums;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Agent.
 */
public class Agent {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(Agent.class);

    /**
     * The Keycloak user.
     */
    private KeycloakUser keycloakUser;

    /**
     * The Associated routing attributes.
     */
    private final Map<String, AssociatedRoutingAttribute> associatedRoutingAttributes = new HashMap<>();
    /**
     * The Active tasks.
     */
    private final Map<String, List<Task>> activeTasks;
    /**
     * The Agent state.
     */
    private AgentState agentState;
    /**
     * The Agent mrd states.
     */
    private final Map<String, AgentMrdState> agentMrdStates;
    /**
     * The Reserved task.
     */
    private Task reservedTask;
    private Task voiceReservedTask;

    /**
     * Default constructor, An Agent object can only be created from a CCUser object.
     *
     * @param ccUser A CCUser object to create the instance from.
     */
    public Agent(@NotNull CCUser ccUser) {
        this.keycloakUser = ccUser.getKeycloakUser();
        if (ccUser.getAssociatedRoutingAttributes() != null) {
            for (AssociatedRoutingAttribute associatedRoutingAttribute : ccUser.getAssociatedRoutingAttributes()) {
                this.associatedRoutingAttributes.put(
                        associatedRoutingAttribute.getRoutingAttribute().getId(),
                        associatedRoutingAttribute);
            }
        }
        this.agentMrdStates = new ConcurrentHashMap<>();
        this.activeTasks = new ConcurrentHashMap<>();
    }

    /**
     * Instantiates a new Agent.
     *
     * @param ccUser     the cc user
     * @param allMrdList the all mrd list
     */
    public Agent(@NotNull CCUser ccUser, @NotNull List<MediaRoutingDomain> allMrdList) {
        this(ccUser);
        this.agentState = new AgentState(Enums.AgentStateName.LOGOUT, null);
        List<AgentMrdState> agentMrdStateList = new ArrayList<>();
        allMrdList.forEach(mrd -> agentMrdStateList.add(new AgentMrdState(mrd, Enums.AgentMrdStateName.NOT_READY)));
        this.setAgentMrdStates(agentMrdStateList);
    }

    /**
     * Update.
     *
     * @param ccUser the cc user
     */
    public void updateFrom(@NotNull CCUser ccUser) {
        this.keycloakUser = ccUser.getKeycloakUser();
        this.associatedRoutingAttributes.clear();
        ccUser.getAssociatedRoutingAttributes().forEach(o ->
                associatedRoutingAttributes.put(o.getRoutingAttribute().getId(), o));
    }

    /**
     * Find associated routing attribute by id associated routing attribute.
     *
     * @param id the id
     * @return the associated routing attribute
     */
    public AssociatedRoutingAttribute findAssociatedRoutingAttributeById(String id) {
        if (id == null) {
            return null;
        }
        return this.associatedRoutingAttributes.get(id);
    }

    /**
     * Assigns a task to the current Agent's object.
     *
     * @param task the task to be added.
     */
    public void assignPushTask(Task task) {
        if (task == null) {
            logger.debug("Cannot assign task, taskService is null");
            return;
        }
        this.removeReservedTask();
        this.addActiveTask(task);
        logger.debug("Agent Id: {}. Task: {} assigned. Total tasks handling: {}.",
                this.keycloakUser.getId(), task.getId(), this.activeTasks.size());
    }

    public void assignExternalTask(Task task) {
        this.voiceReservedTask = null;
        this.addActiveTask(task);
    }

    /**
     * Add a task to the Active tasks list.
     *
     * @param task task to be added
     */
    public void addActiveTask(Task task) {
        String mrdId = task.getMrd().getId();
        this.activeTasks.computeIfAbsent(mrdId, k -> Collections.synchronizedList(new ArrayList<>()));
        List<Task> taskList = this.activeTasks.get(mrdId);
        if (!taskList.contains(task)) {
            taskList.add(task);
        }
    }

    /**
     * Ends task assigned to the current Agent's object.
     *
     * @param task the task to end.
     */
    public void removeTask(Task task) {
        String mrdId = task.getMrd().getId();
        List<Task> taskList = this.activeTasks.get(mrdId);
        if (taskList.contains(task)) {
            if (task.getStartTime() != null) {
                task.setHandlingTime(System.currentTimeMillis() - task.getStartTime());
            } else {
                task.setHandlingTime(0L);
            }
            task.setStartTime(System.currentTimeMillis());
            taskList.remove(task);
        }
    }

    /**
     * Returns total number of active tasks of PUSH routing-mode on an agent's mrd.
     *
     * @param mrdId id of the mrd.
     * @return total number of active tasks on an agent's mrd
     */
    public int getNoOfActivePushTasks(String mrdId) {
        List<Task> taskList = this.activeTasks.get(mrdId);
        if (taskList == null) {
            return 0;
        }
        int counter = 0;
        for (Task task : taskList) {
            if (task.getRoutingMode().equals(RoutingMode.PUSH)) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Return list of all tasks from all MRDs.
     *
     * @return list of all tasks from all MRDs
     */
    public List<Task> getAllTasks() {
        List<Task> result = this.getActiveTasksList();
        if (reservedTask != null) {
            result.add(reservedTask);
        }
        if (voiceReservedTask != null) {
            result.add(voiceReservedTask);
        }
        return result;
    }

    /**
     * Gets active tasks list.
     *
     * @return the active tasks list
     */
    public List<Task> getActiveTasksList() {
        List<Task> result = new ArrayList<>();
        this.activeTasks.forEach((k, v) -> result.addAll(v));
        return result;
    }

    /**
     * Clear all tasks.
     */
    public void clearAllTasks() {
        this.activeTasks.replaceAll((i, v) -> Collections.synchronizedList(new ArrayList<>()));
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public UUID getId() {
        return this.keycloakUser.getId();
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public AgentState getState() {
        return this.agentState;
    }

    /**
     * Sets the Agent's state.
     *
     * @param state the state to be set
     */
    public void setState(AgentState state) {
        this.agentState = state;
    }

    /**
     * Gets agent mrd states.
     *
     * @return the agent mrd states
     */
    public List<AgentMrdState> getAgentMrdStates() {
        List<AgentMrdState> agentMrdStateList = new ArrayList<>();
        this.agentMrdStates.forEach((k, v) -> agentMrdStateList.add(v));
        return agentMrdStateList;
    }

    /**
     * Sets agent mrd states.
     *
     * @param agentMrdStateList the agent mrd states
     */
    public void setAgentMrdStates(List<AgentMrdState> agentMrdStateList) {
        this.agentMrdStates.clear();
        agentMrdStateList.forEach(agentMrdState ->
                this.agentMrdStates.put(agentMrdState.getMrd().getId(), agentMrdState));
    }

    /**
     * Returns the Agent Mrd state, null if not found.
     *
     * @param mrdId id the mrd.
     * @return the Agent Mrd state, null if not found.
     */
    public AgentMrdState getAgentMrdState(String mrdId) {
        if (mrdId == null) {
            return null;
        }
        return this.agentMrdStates.get(mrdId);
    }

    /**
     * Add agent mrd state.
     *
     * @param agentMrdState the agent mrd state
     */
    public void addAgentMrdState(AgentMrdState agentMrdState) {
        this.agentMrdStates.put(agentMrdState.getMrd().getId(), agentMrdState);
    }

    /**
     * Delete agent mrd state.
     *
     * @param mrdId the mrd id
     */
    public void deleteAgentMrdState(String mrdId) {
        if (mrdId != null) {
            this.agentMrdStates.remove(mrdId);
        }
    }

    /**
     * Reserve task.
     *
     * @param task the task
     */
    public void reserveTask(Task task) {
        this.reservedTask = task;
    }

    /**
     * Remove reserved task.
     */
    public void removeReservedTask() {
        this.reservedTask = null;
    }

    /**
     * Is task reserved boolean.
     *
     * @return the boolean
     */
    public boolean isTaskReserved() {
        return this.reservedTask != null;
    }

    public Task getReservedTask() {
        return this.reservedTask;
    }

    public Task getVoiceReservedTask() {
        return voiceReservedTask;
    }

    public void setVoiceReservedTask(Task voiceReservedTask) {
        this.voiceReservedTask = voiceReservedTask;
    }

    /**
     * Returns the last ready state change time for an associated mrd state.
     *
     * @param mrdId id of the mrd
     * @return the last ready state change time for an associated mrd state, null if id not found
     */
    public Timestamp getLastReadyStateChangeTimeFor(@NotNull String mrdId) {
        AgentMrdState agentMrdState = this.agentMrdStates.get(mrdId);
        if (agentMrdState == null) {
            return null;
        }
        return agentMrdState.getStateChangeTime();
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
        ccUser.setAssociatedRoutingAttributes(getAssociatedRoutingAttributesList());
        return ccUser;
    }

    /**
     * Gets associated routing attributes list.
     *
     * @return the associated routing attributes list
     */
    private List<AssociatedRoutingAttribute> getAssociatedRoutingAttributesList() {
        List<AssociatedRoutingAttribute> associatedRoutingAttributeList = new ArrayList<>();
        this.associatedRoutingAttributes.forEach((k, v) -> associatedRoutingAttributeList.add(v));
        return associatedRoutingAttributeList;
    }

    /**
     * Gets associated routing attributes.
     *
     * @return the associated routing attributes
     */
    public Map<String, AssociatedRoutingAttribute> getAssociatedRoutingAttributes() {
        return associatedRoutingAttributes;
    }

    /**
     * Is this agent is available for routing.
     *
     * @param mrdId the mrd id
     * @return the boolean
     */
    public boolean isAvailableForRouting(String mrdId) {
        Enums.AgentStateName agentStateName = this.agentState.getName();
        Enums.AgentMrdStateName mrdState = this.getAgentMrdState(mrdId).getState();

        // (Agent State is ready) AND (AgentMrdState is ready OR active) AND (No task is reserved for this agent)
        // Only one task can be *reserved* for an Agent at a time.
        return agentStateName.equals(Enums.AgentStateName.READY)
                && (mrdState.equals(Enums.AgentMrdStateName.ACTIVE)
                || mrdState.equals(Enums.AgentMrdStateName.READY))
                && !this.isTaskReserved();
    }
}