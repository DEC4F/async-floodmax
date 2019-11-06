package edu.utdalas.cs6380;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The master threads that controls the leader election
 */
class MasterThread implements Runnable {

    //////////////////////////////////
    // FIELDS
    //////////////////////////////////
    
    private final int capacity = 10;
    private int n;
    private int[] ids;
    private int[][] adj;
    private AsyncThread[] threads;
    private boolean finished = false;
    static int leaderID = -1;


    //////////////////////////////////
    // CONSTRUCTOR
    //////////////////////////////////

    MasterThread(int n, int[] ids, int[][] adj) {
        this.n = n;
        this.ids = ids;
        this.adj = adj;
    }

    //////////////////////////////////
    // INTERFACE
    //////////////////////////////////

    @Override
    public void run() {
        try {
            // spawn n threads, assign id and channels
            spawnThreads();
            // run n thread till leader is elected
            runThreads();
            // wait for info computed by async threads
            waitForInfo();
            // print required information
            printInfo();
            finished = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////
    // HELPERS
    //////////////////////////////////

    /**
     * spawns n threads, set up id and channels
     * @throws ThreadException any exception specified in exception class
     */
    private void spawnThreads() throws ThreadException {
        assert n == ids.length : "THE NUMBER OF THREAD SPECIFIED IN INPUT.DAT DOES NOT MATCH WITH THE NUMBER OF ID PROVIDED";

        // set up the two arrays that hold the threads and msg passing channels respectively
        threads = new AsyncThread[n];

        // init individual SyncThread with their own id, receving channel, sending channel and leader id
        for (int i = 0; i < n; ++i) {
            threads[i] = new AsyncThread(ids[i]);
        }
        // set up channels between processes
        setUpTokenChannels(n, capacity);
    }

    /**
     * run n threads and print leader information
     * @throws InterruptedException any exception specified in exception class
     */
    private void runThreads() throws InterruptedException {
        for (AsyncThread thread : threads) {
            new Thread(thread).start();
        }
    }

    /**
     * set up message passing channels between neighboring processes
     * @param n the number of nodes in total
     * @param capacity the max capacity of msg in blocking queue
     */
    private void setUpTokenChannels(int n, int capacity) {
        for (int i = 0; i < n; ++i) {
            // get neighbors of proc i
            // only need to look half of the matrix
            for (int j = i+1; j < n; ++j) {
                if (adj[i][j] == 1) {
                    BlockingQueue<Token> itoj = new ArrayBlockingQueue<>(capacity);
                    BlockingQueue<Token> jtoi = new ArrayBlockingQueue<>(capacity);
                    threads[i].addNeighbor(threads[j], itoj, jtoi);
                    threads[j].addNeighbor(threads[i], jtoi, itoj);
                }
            }
        }
    }

    /**
     * waits for all threads to finish their work
     */
    private void waitForInfo() {
        while (true) {
            boolean done = true;
            for (AsyncThread thread : threads) {
                done = done && thread.getFinished();
            }
            if (done)
                break;
        }
    }

    /**
     * print leader ID and total message transmitted in the election
     */
    private void printInfo() {
        assert leaderID != -1;
        // calc total msg sent
        int totalMsg = 0;
        for (AsyncThread t : threads) {
            totalMsg += t.getMsgSent();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Master Thread: LeaderID = ").append(leaderID).append(". Total number of messages sent = ").append(Integer.toString(totalMsg));
        System.out.println(sb.toString());
    }

    boolean getFinished() {
        return finished;
    }

}
