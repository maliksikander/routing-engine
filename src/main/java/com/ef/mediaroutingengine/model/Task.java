package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.CCUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task {
    private final UUID id;
    private String topicId;
    private String state;
    private List<CCUser> taskAssignee;

    public Task(){
        this.id = UUID.randomUUID();
        this.taskAssignee = new ArrayList<>();
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<CCUser> getTaskAssignee() {
        return taskAssignee;
    }

    public void setTaskAssignee(List<CCUser> taskAssignee) {
        this.taskAssignee = taskAssignee;
    }
}
