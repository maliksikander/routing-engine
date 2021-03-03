package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;

public class Expression {

    private List<Term> terms;
    private String preExpressionCondition;

    public Expression() {
        this.terms = new ArrayList<>();
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    public boolean containsTerm(Term term) {
        return this.terms.contains(term);
    }

    public boolean addTerm(Term term) {
        return this.terms.add(term);
    }

    public boolean removeTerm(Term term) {
        return this.terms.remove(term);
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
