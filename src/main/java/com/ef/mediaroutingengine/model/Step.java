package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.services.ExpressionUtility;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Step {
    private static final Logger LOGGER = LoggerFactory.getLogger(Step.class);
    private List<Expression> expressions;
    private int timeout;
    private List<Agent> associatedAgents;

    public Step() {
        this.expressions = new ArrayList<>();
        this.associatedAgents = new ArrayList<>();
    }

    /**
     * Parameterized Constructor. Constructs object from StepEntity object.
     *
     * @param stepEntity Step entity object in configuration DB
     */
    public Step(StepEntity stepEntity) {
        this.expressions = toExpressions(stepEntity.getExpressions());
        this.timeout = stepEntity.getTimeout();
        this.associatedAgents = new ArrayList<>();
    }

    private List<Expression> toExpressions(List<ExpressionEntity> expressionEntities) {
        if (expressionEntities == null) {
            return new ArrayList<>();
        }
        List<Expression> elements = new ArrayList<>();
        for (ExpressionEntity entity : expressionEntities) {
            elements.add(new Expression(entity));
        }
        return elements;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void evaluateAssociatedAgents(List<Agent> allAgents) {
        this.associatedAgents = ExpressionUtility.evaluateInfix(getInfixExpression(allAgents));
    }

    private List<Object> getInfixExpression(List<Agent> allAgents) {
        List<Object> infixExpression = new ArrayList<>();

        for (Expression expression : this.expressions) {
            if (expression.getPreExpressionCondition() != null) { // e.g. AND (exp1) -> operator before expression.
                infixExpression.add(expression.getPreExpressionCondition());
            }
            infixExpression.add("(");
            infixExpression.addAll(getTermInfixExpression(expression, allAgents));
            infixExpression.add(")");
        }

        return infixExpression;
    }

    private List<Object> getTermInfixExpression(Expression expression, List<Agent> allAgents) {
        List<Object> infixExpression = new ArrayList<>();
        for (Term term : expression.getTerms()) {
            if (term.getPreTermLogicalOperator() != null) { // e.g. AND term1 -> operator before term.
                infixExpression.add(term.getPreTermLogicalOperator());
            }
            infixExpression.add(term.evaluateAssociatedAgents(allAgents));
        }
        return infixExpression;
    }

    public List<Agent> getAssociatedAgents() {
        return associatedAgents;
    }

    /**
     * Removes associated agent by id.
     *
     * @param id id of the agent to be removed
     * @return true if found and removed, false otherwise
     */
    public boolean removeAssociatedAgent(UUID id) {
        int index = -1;
        for (int i = 0; i < associatedAgents.size(); i++) {
            if (associatedAgents.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            associatedAgents.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Sorts and returns the associated agent list by Agent-Selection-Criteria.
     *
     * @param agentSelectionCriteria The selection criteria on which to sort the agents
     * @return the sorted or ordered associated agents list.
     */
    public List<Agent> orderAgentsBy(AgentSelectionCriteria agentSelectionCriteria) {
        switch (agentSelectionCriteria) {
            case LONGEST_AVAILABLE:
                return sortAsLongestAvailable(this.associatedAgents);
            case MOST_SKILLED:
            case LEAST_SKILLED:
            case DEFAULT:
                return this.associatedAgents;
            default:
                LOGGER.error("Switch's default case, returning null");
                return new ArrayList<>();
        }
    }

    private List<Agent> sortAsLongestAvailable(List<Agent> toBeSorted) {
        List<Agent> sorted = new ArrayList<>(toBeSorted);
        //sorted.sort(Comparator.comparing(CCUser::getReadyStateChangeTime));
        return sorted;
    }
}
