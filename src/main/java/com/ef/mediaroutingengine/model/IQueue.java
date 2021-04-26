package com.ef.mediaroutingengine.model;

public interface IQueue {
    boolean enqueue(TaskService task);

    TaskService dequeue();

    void printQueue();

    void logAllSteps();
}
