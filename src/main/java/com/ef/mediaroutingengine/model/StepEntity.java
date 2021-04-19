package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;

public class StepEntity {

    private List<ExpressionEntity> expressions;
    private int timeout;

    public StepEntity() {
        this.expressions = new ArrayList<>();
    }

    public List<ExpressionEntity> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<ExpressionEntity> expressionEntities) {
        this.expressions = expressionEntities;
    }

    public boolean containsExpression(ExpressionEntity expressionEntity) {
        return this.expressions.contains(expressionEntity);
    }

    public boolean addExpression(ExpressionEntity expressionEntity) {
        return this.expressions.add(expressionEntity);
    }

    public boolean removeExpression(ExpressionEntity expressionEntity) {
        return this.expressions.remove(expressionEntity);
    }

    public int getTimeout() {
        return timeout;
    }

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
