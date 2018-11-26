package com.company;

public class PoolSleepLock {
    private int numberOfThreads;
    private BlockingQueue<PendingAction> donePendingActions;

    PoolSleepLock(int numberOfThreads, BlockingQueue<PendingAction> donePendingActions) {
        this.numberOfThreads = numberOfThreads;
        this.donePendingActions = donePendingActions;
    }

    public synchronized void sleep() {
        while (numberOfThreads <= donePendingActions.size()) {
            try{
                wait();
            } catch (Exception ex) {

            }
        }
    }

    public synchronized void wakeUp() {
        if(numberOfThreads > donePendingActions.size()) {
            notify();
        }
    }


}
