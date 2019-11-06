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
                break;
            }
            else if (token.getTokenType().equals(TokenType.EXPLORE)) {
                recvExplore(token);
            }
            else if (token.getTokenType().equals(TokenType.DUMMY)) {
                continue;
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
    private void recvExplore(Token token) throws InterruptedException, ThreadException {
        // flood explore token
        if (token.getMaxID() > UID) {
            maxUID = token.getMaxID();
            parentUID = token.getSenderID();
            flood(new Token(UID, maxUID, TokenType.EXPLORE, round));
        }
        // reply(NACK)
        else if (token.getMaxID() < UID) {
            sendReject(token.getSenderID());
        }
        // impossible to receive own UID from neighbor
        else
            throw new ThreadException(ErrorCode.RECEIVING_OWN_UID_FROM_NEIGHBOR);
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
    private void sendReject (int senderID) throws InterruptedException {
        Token rejectToken = new Token(UID, maxUID, TokenType.REJECT, round);
        Thread.sleep(delay());
        sendChannels.get(senderID).put(rejectToken);
        msgSent ++;
    }

    /**
     * send complete to parent
     */
    private void sendComplete () {

    }

    /**
     * send dummy msg if no msg send to that neighbor at this round
     * @param neighborUID
     * @throws InterruptedException
     */
    private void sendDummy (int neighborUID) throws InterruptedException {
        Token dummyToken = new Token(UID, maxUID, TokenType.DUMMY, round);
        Thread.sleep(delay());
        sendChannels.get(neighborUID).put(dummyToken);
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