package com.company;

import java.util.LinkedList;
import java.util.List;

public class PendingActionsPool extends Thread{
    private int numberOfThreads;
    private List<PendingAction> pendingActions = new LinkedList<>();
    private BlockingQueue<PendingAction> donePendingActions = new BlockingQueue<>();
    private PoolSleepLock poolSleepLock;

    PendingActionsPool(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
        poolSleepLock = new PoolSleepLock(numberOfThreads, donePendingActions);
    }

    public void run() {

        for(int i=0; i < numberOfThreads; ++i) {
            new PendingActionProcessor(donePendingActions, poolSleepLock).start();
        }

        while (true) {
            List<PendingAction> currentReadyToProcess = new LinkedList<>();
            for (PendingAction action : pendingActions) {
                if (action.getState().isDone()) {
                    try{
                        currentReadyToProcess.add(action);
                        donePendingActions.enqueue(action);
                    } catch (Exception ex) {

                    }
                }
            }

            synchronized (this){
                pendingActions.removeAll(currentReadyToProcess);
            }

            poolSleepLock.sleep();
        }
    }

    public void add(PendingAction newOne) {
        synchronized (this) {
            pendingActions.add(newOne);
        }
    }
}

