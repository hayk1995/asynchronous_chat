package com.company;

public class PendingActionProcessor extends Thread{
    private BlockingQueue<PendingAction> readyActions;
    private PoolSleepLock poolSleepLock;
    PendingActionProcessor(BlockingQueue<PendingAction> readyActions, PoolSleepLock poolSleepLock){
        this.readyActions = readyActions;
        this.poolSleepLock = poolSleepLock;
    }

    @Override
    public void run(){
        while (true){
            try {
                PendingAction action = readyActions.deque();
                poolSleepLock.wakeUp();
                action.runHandler();
            } catch (Exception ex) {

            }
        }
    }
}

