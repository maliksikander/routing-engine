package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.commons.Enums;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agent {
    private static final Logger LOGGER = LoggerFactory.getLogger(Agent.class);

    private final KeycloakUser keycloakUser;
    private final List<AssociatedRoutingAttribute> associatedRoutingAttributes;
    private AgentState agentState;
    private List<AgentMrdState> agentMrdStates;
    private final Map<UUID, List<Task>> activeTasks;
    private Task reservedTask;

    /**
     * Default constructor, An Agent object can only be created from a CCUser object.
     *
     * @param ccUser A CCUser object to create the instance from.
     */
    public Agent(@NotNull CCUser ccUser) {
        this.keycloakUser = ccUser.getKeycloakUser();
        this.associatedRoutingAttributes = ccUser.getAssociatedRoutingAttributes();
        this.activeTasks = new ConcurrentHashMap<>();
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
        this.removeReservedTask();
        UUID mrdId = task.getMrd().getId();
        this.activeTasks.computeIfAbsent(mrdId, k -> Collections.synchronizedList(new ArrayList<>()));
        List<Task> taskList = this.activeTasks.get(mrdId);
        if (!taskList.contains(task)) {
            taskList.add(task);
        }

        LOGGER.debug("Agent Id: {}. Task: {} assigned. Total tasks handling: {}.",
                this.keycloakUser.getId(), task.getId(), this.activeTasks.size());
    }

    /**
     * Sets the Mrd states of the Agent.
     *
     * @param updated the list of updated Mrd states
     */
    public void setMrdStates(List<AgentMrdState> updated) {
        for (AgentMrdState newMrdState : updated) {
            for (AgentMrdState oldMrdState : this.agentMrdStates) {
                if (oldMrdState.getMrd().equals(newMrdState.getMrd())) {
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
        UUID mrdId = task.getMrd().getId();
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
        LOGGER.debug("Agent Id: {}. Task : {} removed.", this.getId(), task.getId());
    }

    /**
     * Returns total number of active tasks on an agent's mrd.
     *
     * @param mrdId id of the mrd.
     * @return total number of active tasks on an agent's mrd
     */
    public int getNoOfActiveTasks(UUID mrdId) {
//        int noOfActiveTasks = 0;
//        List<Task> taskList = this.activeTasks.get(mrdId);
//        if (taskList == null) {
//            return noOfActiveTasks;
//        }
//        for (Task task : taskList) {
//            if (task.getTaskState().getName().equals(Enums.TaskStateName.ACTIVE)) {
//                noOfActiveTasks++;
//            }
//        }
//        return noOfActiveTasks;
        List<Task> taskList = this.activeTasks.get(mrdId);
        if (taskList == null) {
            return 0;
        }
        return taskList.size();
    }

    /**
     * Return list of all tasks from all MRDs.
     *
     * @return list of all tasks from all MRDs
     */
    public List<Task> getAllTasks() {
        List<Task> result = new ArrayList<>();
        for (Map.Entry<UUID, List<Task>> entry : this.activeTasks.entrySet()) {
            result.addAll(entry.getValue());
        }
        if (reservedTask != null) {
            result.add(reservedTask);
        }
        return result;
    }

    public void clearAllTasks() {
        this.activeTasks.replaceAll((i, v) -> Collections.synchronizedList(new ArrayList<>()));
    }

    public UUID getId() {
        return this.keycloakUser.getId();
    }

    public List<AssociatedRoutingAttribute> getAssociatedRoutingAttributes() {
        return this.associatedRoutingAttributes;
    }

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

    public List<AgentMrdState> getAgentMrdStates() {
        return agentMrdStates;
    }

    public void setAgentMrdStates(List<AgentMrdState> agentMrdStates) {
        this.agentMrdStates = agentMrdStates;
    }

    /**
     * Returns the Agent Mrd state, null if not found.
     *
     * @param mrdId id the mrd.
     * @return the Agent Mrd state, null if not found.
     */
    public AgentMrdState getAgentMrdState(UUID mrdId) {
        for (AgentMrdState agentMrdState : this.agentMrdStates) {
            if (agentMrdState.getMrd().getId().equals(mrdId)) {
                return agentMrdState;
            }
        }
        return null;
    }

    /**
     * Sets an Agent MRD state.
     *
     * @param mrdId             id of the mrd.
     * @param agentMrdStateName Mrd State to set
     */
    public void setAgentMrdState(UUID mrdId, Enums.AgentMrdStateName agentMrdStateName) {
        // TODO: Change Mrd state list to map.
        for (AgentMrdState agentMrdState : this.agentMrdStates) {
            if (agentMrdState.getMrd().equals(mrdId)) {
                agentMrdState.setState(agentMrdStateName);
                agentMrdState.setStateChangeTime(LocalDateTime.now());
                break;
            }
        }
    }

    public void reserveTask(Task task) {
        this.reservedTask = task;
    }

    public void removeReservedTask() {
        this.reservedTask = null;
    }

    public boolean isTaskReserved() {
        return this.reservedTask != null;
    }

    /**
     * Returns the last ready state change time for an associated mrd state.
     *
     * @param mrdId id of the mrd
     * @return the last ready state change time for an associated mrd state, null if id not found
     */
    public LocalDateTime getLastReadyStateChangeTimeFor(UUID mrdId) {
        for (AgentMrdState agentMrdState : this.agentMrdStates) {
            if (agentMrdState.getMrd().getId().equals(mrdId)) {
                return agentMrdState.getStateChangeTime();
            }
        }
        return null;
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
