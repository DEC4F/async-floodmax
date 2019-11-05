package edu.utdalas.cs6380;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

class AsyncThread implements Runnable {

    enum Status {
        UNKNOWN,
        LEADER,
        NONLEADER
    }

    private int UID;
    private int maxUID;
    private Status status;
    private int round;
    private List<BlockingQueue<Token>> sendChannels;
    private List<BlockingQueue<Token>> recvChannels;

    AsyncThread(int ID) {
        this.UID = ID;
        this.maxUID = ID;
        this.status = Status.UNKNOWN;
        this.round = 0;
        this.sendChannels = new ArrayList<>();
        this.recvChannels = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {

        }
    }

    void addNeighbor(BlockingQueue<Token> sendChan, BlockingQueue<Token> recvChan) {
        sendChannels.add(sendChan);
        recvChannels.add(recvChan);
    }
    
    private void recv() {
        for (BlockingQueue<Token> chann : recvChannels) {
            
        }
    }

    private void send() {

    }

}