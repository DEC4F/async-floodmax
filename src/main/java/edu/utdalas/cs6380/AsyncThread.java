package edu.utdalas.cs6380;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.Random;

import edu.utdalas.cs6380.ThreadException.ErrorCode;
import edu.utdalas.cs6380.Token.TokenType;

class AsyncThread implements Runnable {

    //////////////////////////////////
    // ENUM
    //////////////////////////////////

    enum Status {
        UNKNOWN, LEADER, NONLEADER
    }

    //////////////////////////////////
    // FIELDS
    //////////////////////////////////

    private int UID;
    private int maxUID;
    private int parentUID;
    private int round;
    private Status status;
    private int numNonParentNeighbors;
    private int msgSent;
    private Map<Integer, BlockingQueue<Token>> sendChannels;
    private Map<Integer, BlockingQueue<Token>> recvChannels;
    private List<Token> recvMsg;

    //////////////////////////////////
    // CONSTRUCTOR
    //////////////////////////////////

    AsyncThread(int ID) {
        UID = ID;
        maxUID = ID;
        status = Status.UNKNOWN;
        round = 1;
        msgSent = 1;
        sendChannels = new HashMap<>();
        recvChannels = new HashMap<>();
        recvMsg = new ArrayList<>();
    }

    //////////////////////////////////
    // INTERFACE
    //////////////////////////////////

    @Override
    public void run() {
        try {
        while (status.equals(Status.UNKNOWN)) {
            if (round == 1)
                flood(new Token(UID, maxUID, TokenType.EXPLORE, round));
            recv();
            round ++;
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addNeighbor(int neighborUID, BlockingQueue<Token> sendChan, BlockingQueue<Token> recvChan) {
        sendChannels.put(neighborUID, sendChan);
        recvChannels.put(neighborUID, recvChan);
        numNonParentNeighbors ++;
    }
    
    //////////////////////////////////
    // HELPERS
    //////////////////////////////////

    /**
     * receive tokens from channels
     */
    private void recv() throws InterruptedException, ThreadException {
        for (Integer neighbor : recvChannels.keySet()) {
            BlockingQueue<Token> chann = recvChannels.get(neighbor);
            while (!chann.isEmpty() && chann.peek().getRoundTag() <= round) {
                recvMsg.add(chann.take());
            }
        }
        parseRecvToken();
    }

    /***
     * read all received msg in this "round"
     * @throws InterruptedException
     * @throws ThreadException
     */
    private void parseRecvToken () throws InterruptedException, ThreadException {
        for (Token token : recvMsg) {
            if (token.getTokenType().equals(TokenType.ANOUNCEMENT) && status.equals(Status.UNKNOWN)) {
                recvAnouncement(token.getMaxID());
                return;
            }
        }
        
        for (Token token : recvMsg) {
            if (token.getTokenType().equals(TokenType.DUMMY))
                recvMsg.remove(token);
        }

        for (Token token : recvMsg) {

        }
            else if (token.getTokenType().equals(TokenType.COMPLETED)) {
                recvComplete(token.getSenderID());
            }
            else {
                recvReject(token.getSenderID());
            }
        }
    }

    /**
     * update status and leaderID, flood neighbors, print leader ID
     * @param leaderID
     * @throws InterruptedException
     */
    private void recvAnouncement(Integer leaderID) throws InterruptedException {
        // validation
        assert maxUID < leaderID;
        // flood anouncement token
        maxUID = leaderID;
        status = Status.NONLEADER;
        flood(new Token(UID, maxUID, TokenType.ANOUNCEMENT, round));
        // print leader info
        StringBuilder sb = new StringBuilder();
        sb.append("Thread ").append(UID).append(": LeaderID = ").append(maxUID).append(". Message sent = ").append(msgSent);
        System.out.println(sb.toString());
    }

    /**
     * update maxUID, flood neighbors if token has greater
     * reply reject if token is smaller
     * @param token
     * @throws InterruptedException
     * @throws ThreadException
     */
    private void recvExplore() throws InterruptedException, ThreadException {
        Boolean hasNewInfo = false;
        for (Token token : recvMsg) {
            if (token.getTokenType().equals(TokenType.EXPLORE)) {
                if (token.getMaxID() > maxUID) {
                    hasNewInfo = true;
                    maxUID = token.getMaxID();
                    parentUID = token.getSenderID();
                    if (numNonParentNeighbors == recvChannels.size())
                        numNonParentNeighbors --;
                }
                else {
                    send
                }
                recvMsg.remove(token);
            }
        }
        if (hasNewInfo) {
            flood(new Token(UID, maxUID, TokenType.EXPLORE, round));
        }
        // flood explore token
       
        // reply(NACK)
        else
            sendReject(token.getSenderID());
    }

    /**
     * 
     * @param senderID
     */
    private void recvComplete(int senderID) {

    }

    /**
     * 
     * @param senderID
     */
    private void recvReject(int senderID) {

    }

    /**
     * flood non-parent neighbors with input token, increment message sent
     * @param token
     * @throws InterruptedException
     */
    private void flood (Token token) throws InterruptedException {
        for (Integer neighbor: sendChannels.keySet()) {
            if (!neighbor.equals(parentUID)) {
                Thread.sleep(delay());
                sendChannels.get(neighbor).put(token);
                msgSent ++;
            }
        }
    }

    /**
     * send reject to smaller token sender
     * @param senderID
     * @throws InterruptedException
     */
    private void send (Token token, int neighborIdx) throws InterruptedException {
        Thread.sleep(delay());
        sendChannels.get(neighborIdx).put(token);
        if (!token.getTokenType().equals(TokenType.DUMMY))
            msgSent ++;
    }

    /**
     * calculates message transmission delay
     * @return
     */
    private int delay() {;
        return new Random().nextInt(10)+1;
    }

    //////////////////////////////////
    // ACCESSORS
    //////////////////////////////////

    int getUID () {
        return UID;
    }

    int getMsgSent() {
        return msgSent;
    }

}