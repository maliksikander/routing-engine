package com.ef.mediaroutingengine.routing.model;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.AssociatedMrd;
import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskAgent;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.mediaroutingengine.routing.utility.TaskUtility;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.constraints.NotNull;

/**
 * The type Agent.
 */
public class Agent {
    /**
     * The Associated routing attributes.
     */

    private final Map<String, AssociatedRoutingAttribute> associatedRoutingAttributes = new HashMap<>();
    /**
     * The Active tasks.
     */
    private final Map<String, List<AgentTask>> activeTasks;
    /**
     * The Agent mrd states.
     */
    private final Map<String, AgentMrdState> agentMrdStates;
    /**
     * The Keycloak user.
     */
    private KeycloakUser keycloakUser;
    /**
     * The Agent state.
     */
    private AgentState agentState;
    /**
     * The Reserved task.
     */
    private AgentTask reservedTask;
    private boolean nonInterruptible;

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

        ccUser.getAssociatedMrds().forEach(associatedMrd -> this.agentMrdStates.get(associatedMrd.getMrdId())
                .setMaxAgentTasks(associatedMrd.getMaxAgentTasks()));
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

        this.updateMaxAgentTaskInAgentMrdStates(ccUser);
    }

    private void updateMaxAgentTaskInAgentMrdStates(CCUser ccUser) {
        ccUser.getAssociatedMrds().forEach(associatedMrd -> {
            AgentMrdState agentMrdState = this.agentMrdStates.get(associatedMrd.getMrdId());
            if (agentMrdState != null && agentMrdState.getMaxAgentTasks() != associatedMrd.getMaxAgentTasks()) {
                agentMrdState.setMaxAgentTasks(associatedMrd.getMaxAgentTasks());
                this.agentMrdStates.put(associatedMrd.getMrdId(), agentMrdState);
            }
        });
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
     * Add a task to the Active tasks list.
     *
     * @param task task to be added
     */
    public void addActiveTask(Task task, TaskMedia taskMedia) {
        this.activeTasks.computeIfAbsent(taskMedia.getMrdId(), k -> Collections.synchronizedList(new ArrayList<>()));
        List<AgentTask> taskList = this.activeTasks.get(taskMedia.getMrdId());

        AgentTask agentTask = new AgentTask(task, taskMedia);
        if (!taskList.contains(agentTask)) {
            taskList.add(agentTask);
        }
    }

    /**
     * Gets Keycloak state.
     *
     * @return the keycloak object
     */
    public KeycloakUser getKeycloakUser() {
        return keycloakUser;
    }

    /**
     * Remove task.
     *
     * @param taskId the task id
     * @param mrdId  the mrd id
     */
    public void removeTask(String taskId, String mrdId) {
        List<AgentTask> agentTasks = this.activeTasks.get(mrdId);

        if (agentTasks == null) {
            return;
        }

        ListIterator<AgentTask> iter = agentTasks.listIterator();
        while (iter.hasNext()) {
            if (iter.next().getTaskId().equals(taskId)) {
                iter.remove();
                break;
            }
        }
    }

    /**
     * Returns total number of active tasks of PUSH routing-mode on an agent's mrd.
     *
     * @param mrdId id of the mrd.
     * @return total number of active tasks on an agent's mrd
     */
    public int getNoOfActiveQueueTasks(String mrdId) {
        List<AgentTask> agentTasks = this.activeTasks.get(mrdId);

        if (agentTasks == null) {
            return 0;
        }

        return (int) agentTasks.stream()
                .filter(t -> t.getTaskType().getMode().equals(Enums.TaskTypeMode.QUEUE)
                        || TaskUtility.isNamedAgentTransfer(t.getTaskType()))
                .count();
    }

    /**
     * returns the count of tasks specific to provided queue.
     *
     * @param queueId the queue id.
     * @return tasks count
     */
    public long getActiveTasksCountByQueueId(String queueId) {
        return activeTasks.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .filter(task -> task.getQueue() != null && task.getQueue().getId().equals(queueId))
                .count();
    }

    /**
     * Return list of all tasks from all MRDs.
     *
     * @return list of all tasks from all MRDs
     */
    public List<AgentTask> getAllTasks() {
        List<AgentTask> result = this.getActiveTasksList();
        if (reservedTask != null) {
            result.add(reservedTask);
        }
        return result;
    }

    /**
     * Gets task by conversation id.
     *
     * @param conversationId the conversation id
     * @return the task by conversation id
     */
    public AgentTask getTaskByConversationId(String conversationId) {
        return this.getAllTasks().stream()
                .filter(t -> t.getConversationId().equals(conversationId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets active tasks list.
     *
     * @return the active tasks list
     */
    public List<AgentTask> getActiveTasksList() {
        List<AgentTask> result = new ArrayList<>();
        this.activeTasks.forEach((k, v) -> result.addAll(v));
        return result;
    }

    /**
     * Clear all tasks.
     */
    public void clearAllTasks() {
        this.activeTasks.replaceAll((i, v) -> Collections.synchronizedList(new ArrayList<>()));
        this.reservedTask = null;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return this.keycloakUser.getId();
    }

    public void setKeycloakUser(KeycloakUser keycloakUser) {
        this.keycloakUser = keycloakUser;
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
     * Reserve task boolean.
     *
     * @param task  the task
     * @param media the media
     * @return the boolean
     */
    public synchronized boolean reserveTask(Task task, TaskMedia media) {
        if (reservedTask == null) {
            this.reservedTask = new AgentTask(task, media);
            return true;
        }

        return false;
    }

    /**
     * Remove reserved task.
     */
    public synchronized void removeReservedTask() {
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

    public AgentTask getReservedTask() {
        return this.reservedTask;
    }

    public boolean isNonInterruptible() {
        return nonInterruptible;
    }

    public void setNonInterruptible(boolean nonInterruptible) {
        this.nonInterruptible = nonInterruptible;
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
        ccUser.setAssociatedMrds(getAssociatedMrdList());
        return ccUser;
    }

    public TaskAgent toTaskAgent() {
        return new TaskAgent(this.getId(), this.keycloakUser.displayName());
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
     * Gets associated MRDs list.
     *
     * @return the associated MRDs list
     */
    private List<AssociatedMrd> getAssociatedMrdList() {
        List<AssociatedMrd> associatedMrdList = new ArrayList<>();
        this.getAgentMrdStates().forEach(
                agentMrdState -> associatedMrdList.add(
                        new AssociatedMrd(agentMrdState.getMrd().getId(), agentMrdState.getMaxAgentTasks()))
        );
        return associatedMrdList;
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
    public boolean isAvailableForReservation(String mrdId, String conversationId) {
        // (Agent State is ready) AND (AgentMrdState is ready OR active) AND (No task is reserved for this agent)
        // Only one task can be *reserved* for an Agent at a time.
        return this.isAvailableForReservation(mrdId)
                && !this.isActiveOn(conversationId);
    }

    /**
     * Is available for reservation boolean.
     *
     * @param mrdId the mrd id
     * @return the boolean
     */
    public boolean isAvailableForReservation(String mrdId) {
        Enums.AgentStateName agentStateName = this.agentState.getName();
        Enums.AgentMrdStateName mrdState = this.getAgentMrdState(mrdId).getState();

        return agentStateName.equals(Enums.AgentStateName.READY)
                && (mrdState.equals(Enums.AgentMrdStateName.ACTIVE)
                || mrdState.equals(Enums.AgentMrdStateName.READY))
                && !this.isTaskReserved()
                && !this.isNonInterruptible();
    }

    boolean isActiveOn(String conversationId) {
        return this.getActiveTasksList().stream()
                .anyMatch(t -> t.getConversationId().equals(conversationId));
    }
}