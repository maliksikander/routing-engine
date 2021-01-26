package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Step {
    private UUID id;
    private List<Expression> expressions;
    private int timeout;

    public Step(){
        this.expressions = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public boolean containsExpression(Expression expression){
        return this.expressions.contains(expression);
    }

    public boolean addExpression(Expression expression){
        return this.expressions.add(expression);
    }

    public boolean removeExpression(Expression expression){
        return this.expressions.remove(expression);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
