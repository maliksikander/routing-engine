package com.ef.mediaroutingengine.services.routing;
//
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//
//import org.apache.commons.collections4.ListUtils;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
///**
// * Created by Awais on 17-Aug-17.
// */
//public class Expression {
//    static Logger log = LogManager.getLogger(Expression.class.getName());
//
//    private final List<Object> expression;
//
//    public Expression() {
//        this.expression = new LinkedList<>();
//    }
//
//    public void add(Object obj) {
//        expression.add(obj);
//    }
//
//    public void add() throws ClassNotFoundException {
//        log.trace(" started");
//        expression.add(new Term("","",false));
//        expression.add(CommonDefs.EXPRESSION_OPERATOR.AND);
//        expression.add(CommonDefs.EXPRESSION_OPERATOR.OR);
//
//        for (Object obj : expression) {
//            if (obj instanceof Term) {
//                Term term = (Term) obj;
//            } else if (obj instanceof CommonDefs.TERM_OPERATOR) {
//                CommonDefs.TERM_OPERATOR opr;
//                opr = (CommonDefs.TERM_OPERATOR) obj;
//            }
//        }
//    }
//
//    public int size() {
//        return expression.size();
//    }
//
//    public List<Object> getExpression() {
//        return this.expression;
//    }
//
//    public List<Agent> getAgentsForExpression() {
//        String thisString = string();
//        log.trace("begin for Expression: {}", thisString);
//
//        List<Agent> result = new LinkedList<>();
//        List<Object> terms = getExpression();
//        List<List<Agent>> operationalList = new LinkedList<>();
//
//        try {
//            for (Object obj : this.expression) {
//                if (obj instanceof Term) {
//                    List<Agent> res = AgentsPool.getInstance().findAgents((Term) obj);
//                    if (res != null) {
//                        operationalList.add(res);
//                    }
//                } else if (obj instanceof CommonDefs.EXPRESSION_OPERATOR) {
//                    // all terms must have same expression between them i.e. AND/OR. Same as Cisco Precision IQueue
//                }
//            }
//
//            if (terms.size() > 1) {
//                Object oprObj = terms.get(1);
//                CommonDefs.EXPRESSION_OPERATOR operator = (CommonDefs.EXPRESSION_OPERATOR) oprObj;
//
//                switch (operator) {
//                    case AND:
//                        if (operationalList.size() > 0) {
//                            result.addAll(operationalList.get(0));
//                            for (List<Agent> agents : operationalList) {
//                                result = ListUtils.intersection(result, agents);
//                            }
//                        }
//                        break;
//                    case OR:
//                        if (operationalList.size() > 0) {
//                            for (List<Agent> agents : operationalList) {
//                                result = ListUtils.union(result, agents);
//                            }
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            } else {
//                result.addAll(operationalList.get(0));
//            }
//
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//
//        String associatedAgents = "[";
//        for (Iterator<?> it = result.iterator(); it.hasNext();) {
//            Agent agent = (Agent)it.next();
//            associatedAgents = associatedAgents + agent.getLoginId() + (it.hasNext() ? "," : "");
//        }
//        associatedAgents = associatedAgents + "]";
//        log.trace("Associated Agents List: {}", associatedAgents);
//        log.trace("end");
//        return result;
//    }
//
//    public String string() {
//        String result = "{";
//        for (Iterator<?> it = expression.iterator(); it.hasNext();) {
//            Object obj = it.next();
//            result = result + obj + (it.hasNext() ? " ": "");
//        }
//        result = result + "}";
//        return result;
//    }
//}
