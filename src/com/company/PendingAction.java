package com.company;

import java.util.concurrent.Future;

public class PendingAction {
    private Future state;
    private Handler handler;
    PendingAction(Future state, Handler handler) {
        this.state = state;
        this.handler = handler;
    }

    public Future getState() {
        return state;
    }

    public void runHandler() {
        try{
            Object nioResult = state.get();
            this.handler.run(nioResult);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
