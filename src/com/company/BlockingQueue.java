package com.company;

import java.util.LinkedList;
import java.util.List;

public class BlockingQueue<T> {
    private static final int DEFAULT_LIMIT = 10;

    int limit;
    private List<T> queue = new LinkedList<>();

    BlockingQueue() {
        this(DEFAULT_LIMIT);
    }

    BlockingQueue(int limit) {
        this.limit = limit;
    }

    public synchronized void enqueue(T item) throws InterruptedException {
        while (queue.size() == limit) {
            wait();
        }

        if (queue.size() == 0) {
            notifyAll();
        }

        queue.add(item);
    }

    public synchronized T deque() throws InterruptedException {
        while (queue.size() == 0) {
            wait();
        }
        if (queue.size() == limit) {
            notifyAll();
        }
        return this.queue.remove(0);
    }

    public int size() {
        return queue.size();
    }

}
