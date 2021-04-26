package com.ef.mediaroutingengine.model;

public class Pair<X, Y> {
    public final X count;
    public final Y maxTime;

    public Pair(X x, Y y) {
        this.count = x;
        this.maxTime = y;
    }
}