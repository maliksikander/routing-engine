package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;

public class Expression {
    private List<Term> terms;

    public Expression(){
        this.terms = new ArrayList<>();
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    public boolean containsTerm(Term term){
        return this.terms.contains(term);
    }

    public boolean addTerm(Term term){
        return this.terms.add(term);
    }

    public boolean removeTerm(Term term){
        return this.terms.remove(term);
    }
}
