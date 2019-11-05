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

    private int n;
    private int[] ids;
    private int[][] adj;
    private AsyncThread[] threads;
    private BlockingQueue<Token>[] tokenChannels;
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
            // spawn n threads, assign id
            spawnThreads();
            // run n thread till leader is elected
            runThreads();
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
        setUpTokenChannels(n, 1);

        // init individual SyncThread with their own id, receving channel, sending channel and leader id
        for (int i = 0; i < ids.length; ++i) {
            AsyncThread newThread = new AsyncThread(ids[i]);
            threads[i] = newThread;
        }
    }

    /**
     * run n threads and print leader information
     * @throws InterruptedException any exception specified in exception class
     */
    private void runThreads() throws InterruptedException {
        
    }

    /**
     * initialize the tokenChannels field
     * @param n the number of nodes
     * @param capacity the max capacity of msg in blocking queue
     */
    private void setUpTokenChannels(int n, int capacity) {

    }

}
