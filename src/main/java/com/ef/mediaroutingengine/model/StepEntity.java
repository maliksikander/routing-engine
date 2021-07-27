package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Step entity.
 */
public class StepEntity {

    /**
     * The Expressions.
     */
    private List<ExpressionEntity> expressions;
    /**
     * The Timeout.
     */
    private int timeout;

    /**
     * Instantiates a new Step entity.
     */
    public StepEntity() {
        this.expressions = new ArrayList<>();
    }

    /**
     * Gets expressions.
     *
     * @return the expressions
     */
    public List<ExpressionEntity> getExpressions() {
        return expressions;
    }

    /**
     * Sets expressions.
     *
     * @param expressionEntities the expression entities
     */
    public void setExpressions(List<ExpressionEntity> expressionEntities) {
        this.expressions = expressionEntities;
    }

    /**
     * Contains expression boolean.
     *
     * @param expressionEntity the expression entity
     * @return the boolean
     */
    public boolean containsExpression(ExpressionEntity expressionEntity) {
        return this.expressions.contains(expressionEntity);
    }

    /**
     * Add expression boolean.
     *
     * @param expressionEntity the expression entity
     * @return the boolean
     */
    public boolean addExpression(ExpressionEntity expressionEntity) {
        return this.expressions.add(expressionEntity);
    }

    /**
     * Remove expression boolean.
     *
     * @param expressionEntity the expression entity
     * @return the boolean
     */
    public boolean removeExpression(ExpressionEntity expressionEntity) {
        return this.expressions.remove(expressionEntity);
    }

    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "Step{"
                + "expressions=" + expressions
                + ", timeout=" + timeout
                + '}';
    }
}
