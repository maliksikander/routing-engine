package com.ef.mediaroutingengine.services.routing;
//
//import com.ef.apps.communications.CommunicationManager;
//import com.ef.apps.entities.commons.CommonDefs;
//import com.ef.apps.entities.commons.Properties;
//import com.ef.apps.entities.handlers.eventHandlers.AllAgentsEvent;
//import com.ef.apps.entities.handlers.eventHandlers.DeleteAgentEvent;
//import com.ef.apps.entities.handlers.eventHandlers.NewAgentEvent;
//import com.ef.apps.entities.handlers.eventHandlers.UpdateAgentEvent;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.ef.apps.entities.handlers.eventHandlers.LeaveConversationEvent;
//import com.ef.apps.entities.handlers.eventHandlers.TransferChat;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.beans.PropertyChangeListener;
//import java.beans.PropertyChangeSupport;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.*;
//
///**
// * Created by Awais on 07-Aug-17.
// */
//
///*
//*  Agents are maintained by agent pool
//*  Every expression and step contains their own list of associated agents
//*  Those lists are just references to the agents in agent pool
//*  So we need to update agent state, attribute etc in agent pool and rest of them are updated accordingly.
//* */
//public class AgentsPool {
//    protected static Logger log = LogManager.getLogger(AgentsPool.class.getName());
//    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
//    private static AgentsPool pool;
//    private static List<Agent> agents;
//    private List<PropertyChangeListener> listeners =  new LinkedList<>();
//    boolean enableReporting = false;
//
//    private AgentsPool(){
//        agents = new LinkedList<Agent>();
//        this.listeners = new LinkedList<>();
//        initialize();
//        init();
//    }
//
//    public static AgentsPool getInstance(){
//        if (pool==null){
//            pool = new AgentsPool();
//        }
//        return pool;
//    }
//
//    private void init(){
//        listeners.add(new NewAgentEvent());
//        listeners.add(new UpdateAgentEvent());
//        listeners.add(new DeleteAgentEvent());
//        listeners.add(new AllAgentsEvent());
//
//        for(PropertyChangeListener listener : this.listeners){
//            this.changeSupport.addPropertyChangeListener(listener);
//        }
//    }
//
//    private void initialize(){
//        this.enableReporting = Properties.getEnableReporting().equalsIgnoreCase("true");
//        listeners.add(new TransferChat());
//        listeners.add(new LeaveConversationEvent());
//
//        for(PropertyChangeListener listener : this.listeners){
//            this.changeSupport.addPropertyChangeListener(listener);
//        }
//    }
//
//    public void addPropertyChangeListener(PropertyChangeListener listener){
//        this.changeSupport.addPropertyChangeListener(listener);
//    }
//
//    public void removePropertyChangeListener(PropertyChangeListener listener){
//        this.changeSupport.removePropertyChangeListener(listener);
//    }
//
//    public void publishAgentAddedCDR(Agent agent){
//        if(!enableReporting)
//            return;
//        String body = "{}";
//        JsonNode node = null;
//        try{
//            ObjectMapper mapper = new ObjectMapper();
//            node = mapper.readTree(body);
//            ((ObjectNode)node).put("AgentId", agent.getLoginId());
//            ((ObjectNode)node).put("FirstName", agent.getFirstName());
//            ((ObjectNode)node).put("LastName", agent.getLastName());
//        }
//        catch (Exception ex){
//            log.debug("Exception while adding elements to node for Kafka CDR. Message" + ex.getMessage());
//        }
//        if(node != null)
//            body = node.toString();
//        if(CommonDefs.getMreStatus())
//            CommunicationManager.publishMessage("NewAgent", body, UUID.randomUUID().toString(),
//            CommonDefs.AMQ_QUEUE_NAME.reporting.name());
//        else
//            CommunicationManager.enqueuePendingMessage("NewAgent", body, UUID.randomUUID().toString(),
//            CommonDefs.AMQ_QUEUE_NAME.reporting.name());
//    }
//
//    public Agent getAgentByLoginName(String loginName){
//        Agent agent = null;
//        for(Iterator<? extends Agent> it = agents.iterator(); it.hasNext();){
//            Agent currentAgent = it.next();
//            if(currentAgent.getLoginName().equals(loginName)){
//                agent = currentAgent; // return by reference
//                break;
//            }
//        }
//        return agent;
//    }
//
//    public Agent getAgentByLoginId(String loginId){
//        Agent agent = null;
//        for(Iterator<? extends Agent> it = agents.iterator(); it.hasNext();){
//            Agent currentAgent = it.next();
//            if(currentAgent.getLoginId().equals(loginId)){
//                agent = currentAgent; // return by reference
//                break;
//            }
//        }
//        return agent;
//    }
//
//    public boolean addAgent(Agent agent){
//        boolean result = false;
//        if(agent!= null && agentExists(agent.getLoginId())){
//            List<Attribute> agentAttrs = this.getAgentByLoginId(agent.getLoginId()).getAttributes();
//            for(Attribute attr : agent.getAttributes()){
//                agentAttrs.add(attr); // set by reference
//            }
//        }
//        else if(agent != null && !agentExists(agent.getLoginId())) {
//            synchronized (this.agents) {
//                result = agents.add(agent);
//                this.publishAgentAddedCDR(agent);
//            }
//        }
//        return result;
//    }
//
//    private boolean agentExists(String agentId){
//        boolean result = false;
//        for(Iterator<? extends Agent> it = agents.iterator(); it.hasNext();){
//            Agent currentAgent = it.next();
//
//            if(currentAgent.getLoginId().equals(agentId)){
//                result = true;
//                break;
//            }
//        }
//        return result;
//    }
//
//    public boolean updateAgent(Agent agent){
//        boolean result = false;
//        for(Agent ag : agents){
//            if(ag.getLoginId().equalsIgnoreCase(agent.getLoginId())){
//                synchronized (agents){
//                    ag.setAttributes(agent.getAttributes());
//                    result = true;
//                }
//            }
//        }
//        return result;
//    }
//
//    public boolean removeAgent(String agentId){
//        boolean result = false;
//        Agent it_agent = null;
//        for(Iterator<? extends Agent> it = agents.iterator(); it.hasNext();){
//            it_agent = it.next();
//            if(it_agent.getLoginId().equals(agentId)){
//                synchronized (agents) {
//                    agents.remove(this.getAgentByLoginId(agentId));
//                }
//                result = true;
//                break;
//            }
//        }
//        return result;
//    }
//
//
//    public List<Agent> findAgents(Term term){
//        List<Agent> result = new LinkedList<Agent>();
//        for(Iterator<? extends Agent> it = agents.iterator(); it.hasNext();){
//            Agent agent = it.next();
//            if(compareAgentToTerm(agent,term)){
//                result.add(agent);
//            }
//        }
//        return result;
//    }
//
//    private boolean compareAgentToTerm(Agent agent, Term term){
//        boolean result = false;
//        CommonDefs.ATTRIBUTE_TYPE type = term.getAttribute().getAttributeType();
//        Attribute agentAttr = agent.getAttributeByName(term.getAttribute().getName());
//        if(agentAttr == null) return false;
//        switch(type){
//            case BOOLEAN:
//                if (term.getAttribute().getAttributeValue() instanceof Boolean &&
//                        (boolean)agentAttr.getAttributeValue() == term.getBooleanValue())
//                    result = true;
//                break;
//            case PROFICIENCY:
//                int agentProficiency = (int)agentAttr.getAttributeValue();
//                CommonDefs.TERM_OPERATOR operator = term.getOperator();
//                int termProficiency = (int)term.getProficiency();
//                switch(operator){
//                    case EQUAL:
//                        result = agentProficiency == termProficiency;
//                        break;
//                    case EQUAL_OR_GRATER:
//                        result = agentProficiency >= termProficiency;
//                        break;
//                    case EQUAL_OR_LESS:
//                        result = agentProficiency <= termProficiency;
//                        break;
//                    case NOT_EQUAL:
//                        result = agentProficiency != termProficiency;
//                        break;
//                    case GRATER:
//                        result = agentProficiency > termProficiency;
//                        break;
//                    case LESS:
//                        result = agentProficiency < termProficiency;
//                        break;
//                        default:
//                            break;
//                }
//                break;
//            default:
//                break;
//        }
//        return result;
//    }
//
//    public List<Agent> getAgents(){
//        return this.agents;
//    }
//
//    public void handleLeaveConversation(JsonNode node) {
//        this.changeSupport.firePropertyChange(CommonDefs.EVENT_PROPERTIES.LeaveConversation.name(), null, node);
//    }
//
//    public void handleTransferCommand(JsonNode node) {
//        this.changeSupport.firePropertyChange(CommonDefs.EVENT_PROPERTIES.Transfer.name(), null, node);
//    }
//
//    public void handleAllAgentsEvent(CommonDefs.EVENT_PROPERTIES property, JsonNode node) {
//        this.changeSupport.firePropertyChange(property.name(), null, node);
//    }
//
//    public void handleNewAgentEvent(CommonDefs.EVENT_PROPERTIES property, JsonNode node) {
//        this.changeSupport.firePropertyChange(property.name(), null, node);
//    }
//
//    public void handleUpdateAgentEvent(CommonDefs.EVENT_PROPERTIES property, JsonNode node) {
//        this.changeSupport.firePropertyChange(property.name(), null, node);
//    }
//
//    public void handleDeleteAgentEvent(CommonDefs.EVENT_PROPERTIES property, JsonNode node) {
//        this.changeSupport.firePropertyChange(property.name(), null, node);
//    }
//}
