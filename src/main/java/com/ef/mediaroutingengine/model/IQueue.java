package com.ef.mediaroutingengine.model;

public interface IQueue {
    boolean enqueue(Task task);

    Task dequeue();

    void printQueue();

    void logAllSteps();
}
