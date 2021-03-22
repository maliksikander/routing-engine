package com.ef.mediaroutingengine.services.routing;
//
//import com.ef.apps.entities.commons.CommonDefs;
//import com.ef.apps.entities.handlers.commandHandlers.GetAgentState;
//import com.ef.apps.entities.handlers.eventHandlers.AgentStateEvent;
//import com.fasterxml.jackson.databind.JsonNode;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.beans.PropertyChangeListener;
//import java.beans.PropertyChangeSupport;
//import java.time.LocalDateTime;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * Created by Awais on 07-Aug-17.
// */
//public class Agent {
//    private static Logger log = LogManager.getLogger(Agent.class.getName());
//    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
//
//    private String extension;
//    private String firstName;
//    private String lastName;
//    private String loginId;
//    private String loginName;
//    private String pendingState;
//    //private ReasonCode reasonCode;
//    private String reasonCodeId;
//    //private Roles roles;
//    //private Settings settings;
//    //private AgentState state;
//    //private Date stateChangeTime;
//    private String teamId;
//    private String teamName;
//    //private Teams teams;
//    private String uri;
//    private String password;
//    private String authString;
//    private CommonDefs.AGENT_STATE agentState;
//    private CommonDefs.AGENT_MODE agentMode = CommonDefs.AGENT_MODE.NON_ROUTEABLE;
//    private AtomicInteger numOfTasks;
//    private LocalDateTime lastReadyStateChangeTime;
//
//    // Event Handlers
//    private List<PropertyChangeListener> listeners;
//
//    private List<Attribute> attributes;
//
//    private List<TaskService> assignedTasks;
//    // Begin - Constructors
//    public Agent(){
//        attributes = new LinkedList<Attribute>();
//        assignedTasks = new LinkedList<>();
//        this.lastReadyStateChangeTime = LocalDateTime.of(1990,4,2,12,1);
//        this.initialize();
//    }
//
//    public Agent(Agent agent){
//        this.extension = agent.extension;
//        this.firstName = agent.firstName;
//        this.lastName = agent.lastName;
//        this.loginId = agent.loginId;
//        this.loginName = agent.loginName;
//        this.pendingState = agent.pendingState;
//        this.reasonCodeId = agent.reasonCodeId;
//        this.teamId = agent.teamId;
//        this.uri = agent.uri;
//        this.password = agent.password;
//        this.authString = agent.authString;
//        this.agentState = agent.agentState;
//        this.numOfTasks = agent.numOfTasks;
//        this.listeners.clear();
//        for(PropertyChangeListener listener :  agent.listeners){
//            this.listeners.add(listener);
//        }
//        this.attributes.clear();
//        for(Attribute attr: agent.attributes){
//            this.attributes.add(attr);
//        }
//    }
//
//    public Agent(String loginId, String password, String extension) {
//        setLoginId(loginId);
//        setPassword(password);
//        setExtension(extension);
//        this.numOfTasks = new AtomicInteger(0);
//        attributes = new LinkedList<Attribute>();
//        assignedTasks = new LinkedList<>();
//        this.lastReadyStateChangeTime = LocalDateTime.of(1990,4,2,12,1);
//        this.initialize();
//    }
//    // End - Constructors
//
//    private void initialize(){
//        listeners = new LinkedList<PropertyChangeListener>();
//        // add commands
//        listeners.add(new GetAgentState());
//
//        // add events
//        listeners.add(new AgentStateEvent());
//
//        for(PropertyChangeListener listener: this.listeners){
//            this.changeSupport.addPropertyChangeListener(listener);
//        }
//    }
//
//    private boolean taskExists(TaskService taskService){
//        boolean result = false;
//        for (TaskService task : this.assignedTasks){
//            if(task.getId().equalsIgnoreCase(taskService.getId())){
//                result = true;
//                break;
//            }
//        }
//        return result;
//    }
//
//    public void assignTask(TaskService taskService){
//        if(taskService == null){
//            log.debug("Cannot assign task, taskService is null");
//            /*
//            * Ramzan
//            * put a breakpoint on line 130 log.debug("cannot assign task, taskService is null");
//            * this never happened before, identify the case when a null task has come to assignment
//            * Do not fix the issue, discuss with me about the problem first
//            * This check is only for debugging, we will not go to production with this null check.
//            * Need to remove it when issue is identified and fixed.
//            * */
//            return;
//        }
//        if(!taskExists(taskService)) {
//            synchronized (this.assignedTasks) {
//                this.assignedTasks.add(taskService);
//            }
//        }
//        log.debug("Agent Id: "+ this.loginId +"Task: " + taskService.getId()
//        + " assigned. Total tasks handling: " + this.assignedTasks.size());
//    }
//    public void assignTask(String taskId){
//        this.assignTask(TaskServiceManager.getInstance().getTask(taskId));
//    }
//
//    public void endTask(TaskService taskService){
//        if(taskExists(taskService)) {
//            taskService.setHandlingTime(taskService.getStartTime() != null
//            ? (System.currentTimeMillis() - taskService.getStartTime()) : 0);
//            taskService.setStartTime(System.currentTimeMillis());
//            synchronized (this.assignedTasks) {
//                this.assignedTasks.remove(taskService);
//            }
//        }
//        log.debug("Agent Id: "+ this.loginId +"Task : " + taskService.getId()
//        + " removed. Total tasks handling: " + this.assignedTasks.size());
//    }
//
//    public void endTask(String taskId){
//        this.endTask(TaskServiceManager.getInstance().getTask(taskId));
//    }
//
//    public void addSchedulerListeners(PropertyChangeListener listener, String name){
//        for(PropertyChangeListener i : this.listeners){
//            if(i instanceof AgentStateEvent)
//                ((AgentStateEvent)i).addPropertyChangeListener(listener, name);
//        }
//    }
//
//    public void handleStateEvent(CommonDefs.EVENT_PROPERTIES property, JsonNode node){
//        this.changeSupport.firePropertyChange(property.name(), null, node);
//    }
//
//    // Begin - Getters Setters
//    public String getExtension(){return this.extension;}
//    public void setExtension(String extension){this.extension = extension;}
//
//    public String getFirstName(){return this.firstName;}
//    public void setFirstName(String firstName){this.firstName = firstName;}
//
//    public String getLastName(){return this.lastName;}
//    public void setLastName(String lastName) {this.lastName = lastName;}
//
//    public String getName(){return firstName + " " + lastName;}
//
//    public String getLoginId(){return this.loginId;}
//    public void setLoginId(String loginId){this.loginId = loginId;}
//
//    public String getLoginName(){return this.loginName;}
//    public void setLoginName(String loginName){this.loginName = loginName;}
//
//    public String getPendingState() {return this.pendingState;}
//    public void setPendingState(String pendingState) {this.pendingState = pendingState;}
//
//    public String getReasonCodeId(){return this.reasonCodeId;}
//    public void setReasonCodeId(String reasonCodeId){this.reasonCodeId = reasonCodeId;}
//
//    public String getTeamId(){return this.teamId;}
//    public void setTeamId(String teamId){this.teamId = teamId;}
//
//    public String getTeamName(){return this.teamName;}
//    public void setTeamName(String teamName){this.teamName = teamName;}
//
//    public String getUri(){return this.uri;}
//    public void setUri(String uri){this.uri = uri;}
//
//    public String getPassword(){return this.password;}
//    public void setPassword(String password){this.password = password;}
//
//    public CommonDefs.AGENT_MODE getAgentMode() { return this.agentMode; }
//    public void setAgentMode(CommonDefs.AGENT_MODE agentMode) {
//        this.agentMode = agentMode;
//    }
//
//    public String getAuthString(){return this.authString;}
//    public void setAuthString(String authString){this.authString = authString;}
//
//    public void setState(CommonDefs.AGENT_STATE state) {
//        CommonDefs.AGENT_STATE previousState = this.agentState;
//        this.agentState = state;
//        if(state == CommonDefs.AGENT_STATE.LOGOUT){
//            synchronized (this.assignedTasks){
//                this.assignedTasks.clear();
//            }
//        }
//        if(state == CommonDefs.AGENT_STATE.READY){
//            //if(previousState != CommonDefs.AGENT_STATE.ACTIVE)
//            this.setReadyStateChangeTime(LocalDateTime.now());
//        }
//    }
//    public CommonDefs.AGENT_STATE getState() { return this.agentState; }
//
//    public void incrementNumOfTasks(){
//        synchronized (numOfTasks) {
//            this.numOfTasks.incrementAndGet();
////            if (numOfTasks.get() > 4) {
////                this.agentState = CommonDefs.AGENT_STATE.BUSY;
////            }
//        }
//    }
//
//    public void decrementNumOfTasks(){
//        synchronized (numOfTasks) {
////            if (this.numOfTasks.get() > 0) {
//                this.numOfTasks.decrementAndGet();
////                System.out.println(this.numOfTasks);
////            }
//        }
//    }
//
//    public int getNumOfTasks(){
//        int result= 0;
//        synchronized (this.assignedTasks)
//        {
//            result = this.assignedTasks.size();
//        }
//        return result;
//    }
//    // End - Getters Setters
//
//
////    public boolean copy(Agent right){
////        //Agent agent = new Agent(right.getLoginId(), right.getPassword(), right.getExtension());
////        boolean result = false;
////        try {
////            this.setLoginId(right.getLoginId());
////            this.setPassword(right.getPassword());
////            this.setExtension(right.getExtension());
////            this.setAuthString(right.getAuthString());
////            this.setFirstName(right.getFirstName());
////            this.setLastName(right.getLastName());
////            this.setLoginName(right.getLoginName());
////            this.setPendingState(right.getPendingState());
////            this.setReasonCodeId(right.getReasonCodeId());
////            this.setTeamId(right.getTeamId());
////            this.setTeamName(right.getTeamName());
////            this.setUri(right.getUri());
////            result = true;
////        }
////        catch(Exception e){
////            System.out.println(e.getMessage());
////        }
////
////        return result;
////    }
//
//    public void associateAttribute(Attribute attribute, Object value){
//        synchronized(this.attributes){
//            Attribute attr = new Attribute(attribute);
//            attr.setAttributeValue(value);
//            this.attributes.add(attr);
//            AttributesPool.getInstance().beginUsingAttribute(attribute.getName());
//        }
//    }
//
//    public boolean removeAttribute(String name){
//        boolean result = false;
//        Attribute attribute = null;
//        for(Iterator<? extends Attribute> it = attributes.iterator(); it.hasNext();){
//            Attribute itr_attr = it.next();
//            if(itr_attr.getName().equalsIgnoreCase(name)){
//                attribute = itr_attr;
//                break;
//            }
//        }
//        if(attribute != null) {
//            attributes.remove(attribute);
//            AttributesPool.getInstance().endUsingAttribute(name);
//            result = true;
//        }
//        return result;
//    }
//
//    public Attribute getAttributeByName(String name){
//        Attribute attr = null;
//        for(Iterator<? extends Attribute> it = attributes.iterator(); it.hasNext();){
//            Attribute i = it.next();
//            if(i.getName().equalsIgnoreCase(name)){
//                attr = i;
//                break;
//            }
//        }
//        return attr;
//    }
//
//    public List<Attribute> getAttributes(){
//        return this.attributes;
//    }
//
//    public void setReadyStateChangeTime(LocalDateTime time) { this.lastReadyStateChangeTime = time;}
//
//    public LocalDateTime getReadyStateChangeTime() { return this.lastReadyStateChangeTime; }
//
//    public List<TaskService> getAssignedTasks() { return this.assignedTasks; }
//    public void setAttributes(List<Attribute> attributes) {
//        this.attributes.clear();
//        this.attributes.addAll(attributes);
//    }
//}
