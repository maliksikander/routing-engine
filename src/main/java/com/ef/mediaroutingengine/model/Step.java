package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.services.ExpressionUtility;
import java.util.ArrayList;
import java.util.List;

public class Step {
    private List<Expression> expressions;
    private int timeout;
    private List<CCUser> associatedAgents;

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
        for (ExpressionEntity entity: expressionEntities) {
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

    public List<CCUser> getAssociatedAgents() {
        return associatedAgents;
    }

    public void setAssociatedAgents(List<CCUser> associatedAgents) {
        this.associatedAgents = associatedAgents;
    }

    public void evaluateAssociatedAgents(List<CCUser> allAgents) {
        this.associatedAgents = ExpressionUtility.evaluateInfix(getInfixExpression(allAgents));
    }

    private List<Object> getInfixExpression(List<CCUser> allAgents) {
        List<Object> infixExpression = new ArrayList<>();

        for (Expression expression: this.expressions) {
            if (expression.getPreExpressionCondition() != null) { // e.g. AND (exp1) -> operator before expression.
                infixExpression.add(expression.getPreExpressionCondition());
            }
            infixExpression.add("(");
            infixExpression.addAll(getTermInfixExpression(expression, allAgents));
            infixExpression.add(")");
        }

        return infixExpression;
    }

    private List<Object> getTermInfixExpression(Expression expression, List<CCUser> allAgents) {
        List<Object> infixExpression = new ArrayList<>();
        for (Term term: expression.getTerms()) {
            if (term.getPreTermLogicalOperator() != null) { // e.g. AND term1 -> operator before term.
                infixExpression.add(term.getPreTermLogicalOperator());
            }
            infixExpression.add(term.evaluateAssociatedAgents(allAgents));
        }
        return infixExpression;
    }
}
