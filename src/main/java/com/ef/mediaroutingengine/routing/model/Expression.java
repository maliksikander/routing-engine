package com.ef.mediaroutingengine.routing.model;

import com.ef.cim.objectmodel.ExpressionEntity;
import com.ef.cim.objectmodel.TermEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Expression.
 */
public class Expression {
    /**
     * The Terms.
     */
    private List<Term> terms;
    /**
     * The Pre expression condition.
     */
    private String preExpressionCondition;

    /**
     * Instantiates a new Expression.
     */
    public Expression() {
        this.terms = new ArrayList<>();
    }

    /**
     * Instantiates a new Expression.
     *
     * @param expressionEntity the expression entity
     */
    public Expression(ExpressionEntity expressionEntity) {
        this.terms = toTerms(expressionEntity.getTerms());
        this.preExpressionCondition = expressionEntity.getPreExpressionCondition();
    }

    /**
     * To terms list.
     *
     * @param termEntities the term entities
     * @return the list
     */
    private List<Term> toTerms(List<TermEntity> termEntities) {
        if (termEntities == null) {
            return new ArrayList<>();
        }
        List<Term> elements = new ArrayList<>();
        for (TermEntity entity : termEntities) {
            elements.add(new Term(entity));
        }
        return elements;
    }

    /**
     * Gets terms.
     *
     * @return the terms
     */
    public List<Term> getTerms() {
        return terms;
    }

    /**
     * Sets terms.
     *
     * @param terms the terms
     */
    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    /**
     * Gets pre expression condition.
     *
     * @return the pre expression condition
     */
    public String getPreExpressionCondition() {
        return preExpressionCondition;
    }

    /**
     * Sets pre expression condition.
     *
     * @param preExpressionCondition the pre expression condition
     */
    public void setPreExpressionCondition(String preExpressionCondition) {
        this.preExpressionCondition = preExpressionCondition;
    }
}
