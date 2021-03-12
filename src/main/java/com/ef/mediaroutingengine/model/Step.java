package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;

public class Step {

    private List<Expression> expressions;
    private int timeout;

    public Step() {
        this.expressions = new ArrayList<>();
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public boolean containsExpression(Expression expression) {
        return this.expressions.contains(expression);
    }

    public boolean addExpression(Expression expression) {
        return this.expressions.add(expression);
    }

    public boolean removeExpression(Expression expression) {
        return this.expressions.remove(expression);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "Step{" +
                "expressions=" + expressions +
                ", timeout=" + timeout +
                '}';
    }
}
