package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;

public class ExpressionEntity {

    private List<TermEntity> terms;
    private String preExpressionCondition;

    public ExpressionEntity() {
        this.terms = new ArrayList<>();
    }

    public List<TermEntity> getTerms() {
        return terms;
    }

    public void setTerms(List<TermEntity> termEntities) {
        this.terms = termEntities;
    }

    public boolean containsTerm(TermEntity termEntity) {
        return this.terms.contains(termEntity);
    }

    public boolean addTerm(TermEntity termEntity) {
        return this.terms.add(termEntity);
    }

    public boolean removeTerm(TermEntity termEntity) {
        return this.terms.remove(termEntity);
    }

    public String getPreExpressionCondition() {
        return preExpressionCondition;
    }

    public void setPreExpressionCondition(String preExpressionCondition) {
        this.preExpressionCondition = preExpressionCondition;
    }

    @Override
    public String toString() {
        return "Expression{"
                + "terms=" + terms
                + ", preExpressionCondition='" + preExpressionCondition + '\''
                + '}';
    }
}
