package edu.utdalas.cs6380;

import java.util.concurrent.BlockingQueue;

class AsyncThread implements Runnable {

    private int ID;
    private BlockingQueue[] channels;

    AsyncThread(int ID) {
        this.ID = ID;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }
    
}