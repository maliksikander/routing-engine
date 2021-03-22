package com.ef.mediaroutingengine.services.routing;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import org.apache.commons.collections4.ListUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class Step {
//
//    private static Logger LOGGER = LoggerFactory.getLogger(Step.class);
//
//    private String name;
//    private List<Object> expressions;
//    private int timeoutInSeconds;
//
//    private List<Agent> associatedAgents;
//    private HashMap<String, Agent> associatedAgentsMap;
//
//    public Step(String name) {
//        this.name = name;
//        expressions = new LinkedList<>();
//        associatedAgents = new LinkedList<>();
//        this.associatedAgentsMap = new LinkedHashMap<>();
//    }
//
//    public void addExpression(Object object) {
//        expressions.add(object);
//    }
//
//    public void setTimeout(int seconds) {
//        this.timeoutInSeconds = seconds;
//    }
//
//    public int getTimeout() {
//        return this.timeoutInSeconds;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void updateAssociatedAgents() {
//        LOGGER.debug("begin");
//        LOGGER.info("Step: {}", this);
//
//        List<List<Agent>> tempOperationalList = new LinkedList<>();
//
//        int i = 0;
//
//        CommonDefs.EXPRESSION_OPERATOR lastOperator = null;
//        // Parse agent list & get agents based upon expressions
//        for (Iterator<?> it = expressions.iterator(); it.hasNext();) {
//            Object obj = it.next();
//            if (obj instanceof Expression) {
//                tempOperationalList.add(((Expression) obj).getAgentsForExpression());
//                if (i == 0) {
//                    synchronized (associatedAgents) {
//                        associatedAgents.clear();
//                        associatedAgents.addAll(tempOperationalList.get(i));
//                    }
//                } else {
//                    i += 0;
//                }
//
//                if (!it.hasNext() && expressions.size() != 1 && lastOperator != null) {
//                    synchronized (associatedAgents) {
//                        switch (lastOperator) {
//                            case AND:
//                                associatedAgents = ListUtils.intersection(associatedAgents,
//                                        tempOperationalList.get(tempOperationalList.size() - 1));
//                                break;
//                            case OR:
//                                associatedAgents = ListUtils.union(associatedAgents,
//                                        tempOperationalList.get(tempOperationalList.size() - 1));
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                }
//            } else if (obj instanceof CommonDefs.EXPRESSION_OPERATOR) {
//
//                lastOperator = (CommonDefs.EXPRESSION_OPERATOR) obj;
//                synchronized (associatedAgents) {
//                    switch (((CommonDefs.EXPRESSION_OPERATOR) obj)) {
//                        case AND:
//                            associatedAgents = ListUtils.intersection(associatedAgents, tempOperationalList.get(i));
//                            break;
//                        case OR:
//                            associatedAgents = ListUtils.union(associatedAgents, tempOperationalList.get(i));
//                            break;
//                        default:
//                            break;
//                    }
//                }
//
//                if (i == 0) {
//                    i += 1;
//                }
//            }
//        }
//        updateAgentsMap();
//        LOGGER.debug("end");
//    }
//
//    public void resetAssociatedAgents() {
//        synchronized (associatedAgents) {
//            synchronized (associatedAgentsMap) {
//                this.associatedAgents.clear();
//                this.associatedAgentsMap.clear();
//            }
//        }
//    }
//
//    private void updateAgentsMap() {
//        synchronized (associatedAgentsMap) {
//            associatedAgentsMap.clear();
//            for (Agent agent : associatedAgents) {
//                associatedAgentsMap.put(agent.getLoginId(), agent);
//            }
//        }
//        StringBuilder agents = new StringBuilder("[");
//        for (Map.Entry<String, Agent> entry : associatedAgentsMap.entrySet()) {
//            agents.append(entry.getKey()).append(",");
//        }
//        agents = new StringBuilder(agents.length() > 0
//        ? agents.substring(0, agents.length() - 1) + "]" : agents + "]");
//        // clear associated agents array, it just consumes space
//        this.associatedAgents.clear();
//        LOGGER.info("associated agents with step {} are: {}", this.name, agents);
//    }
//
//    public String toString() {
//        StringBuilder step = new StringBuilder("[");
//        for (Object obj : expressions) {
//            if (obj instanceof Expression) {
//                step.append(((Expression) obj).string());
//            } else if (obj instanceof CommonDefs.EXPRESSION_OPERATOR) {
//                step.append(" ").append(obj).append(" ");
//            }
//        }
//        step.append("]");
//        return step.toString();
//    }
//
//    public int getAssociatedAgentsCount() {
//        return this.associatedAgentsMap.size();
//    }
//
//    public long getAssociatedAgentsCount(CommonDefs.AGENT_STATE agentState) {
//        List<Agent> agentsList = new ArrayList<>(this.associatedAgentsMap.values());
//        return agentsList.stream().filter(agent -> agent.getState() == agentState).count();
//    }
//
//    public LinkedHashMap<String, Agent> getAssociatedAgents(PrecisionQueue.AGENT_ORDER agentOrder) {
//        switch (agentOrder) {
//            case MOST_SKILLED:
//                return (LinkedHashMap<String, Agent>) this.associatedAgentsMap;
//            case LEAST_SKILLED:
//                return (LinkedHashMap<String, Agent>) this.associatedAgentsMap;
//            case LONGEST_AVAILABLE:
//                LinkedHashMap<String, Agent> result = new LinkedHashMap<>();
//                List<Agent> assAgentsList = new ArrayList<>(this.associatedAgentsMap.values());
//                try {
//                    assAgentsList.sort(Comparator.comparing(Agent::getReadyStateChangeTime));
//                } catch (Exception ex) {
//                    LOGGER.warn("Exception in getAssociatedAgents(). Message: {}", ex.getMessage());
//                }
//                for (Agent associatedAgent : assAgentsList) {
//                    result.put(associatedAgent.getLoginId(), associatedAgent);
//                }
//                return result;
//            case DEFAULT:
//                return (LinkedHashMap<String, Agent>) this.associatedAgentsMap;
//            default:
//                return (LinkedHashMap<String, Agent>) this.associatedAgentsMap;
//        }
//    }
//}
