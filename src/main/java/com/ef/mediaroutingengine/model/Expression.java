package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;

public class Expression {
    private List<Term> terms;
    private String preExpressionCondition;

    public Expression() {
        this.terms = new ArrayList<>();
    }

    public Expression(ExpressionEntity expressionEntity) {
        this.terms = toTerms(expressionEntity.getTerms());
        this.preExpressionCondition = expressionEntity.getPreExpressionCondition();
    }

    private List<Term> toTerms(List<TermEntity> termEntities) {
        if (termEntities == null) {
            return new ArrayList<>();
        }
        List<Term> elements = new ArrayList<>();
        for (TermEntity entity: termEntities) {
            elements.add(new Term(entity));
        }
        return elements;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    public String getPreExpressionCondition() {
        return preExpressionCondition;
    }

    public void setPreExpressionCondition(String preExpressionCondition) {
        this.preExpressionCondition = preExpressionCondition;
    }
}
