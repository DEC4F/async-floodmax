package edu.utdalas.cs6380;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.Random;

import edu.utdalas.cs6380.ThreadException.ErrorCode;
import edu.utdalas.cs6380.Token.TokenType;

class AsyncThread implements Runnable {

    enum Status {
        UNKNOWN, LEADER, NONLEADER
    }

    private int UID;
    private int maxUID;
    private int parentUID;
    private int round;
    private Status status;
    private int msgSent;
    private Map<Integer, BlockingQueue<Token>> sendChannels;
    private Map<Integer, BlockingQueue<Token>> recvChannels;

    AsyncThread(int ID) {
        this.UID = ID;
        this.maxUID = ID;
        this.status = Status.UNKNOWN;
        this.round = 1;
        this.msgSent = 1;
        this.sendChannels = new HashMap<>();
        this.recvChannels = new HashMap<>();
    }

    @Override
    public void run() {
        try {
        while (status.equals(Status.UNKNOWN)) {
            if (round == 1)
                sendExplore();
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
    
    private void recv() throws InterruptedException, ThreadException {
        // set expected number of msg to be recv
        int numNeighbors = recvChannels.size();

        for (Integer neighbor : recvChannels.keySet()) {
            if (recvChannels.isEmpty())
                continue;
            Token token = recvChannels.get(neighbor).take();
            if (token.getTokenType().equals(TokenType.ANOUNCEMENT)) {
                recvAnouncement(token.getMaxID());
                break;
            }
            else if (token.getTokenType().equals(TokenType.DUMMY)) {
                continue;
            }
            else if (token.getTokenType().equals(TokenType.COMPLETED)) {
                recvComplete(token.getSenderID());
            }
            else if (token.getTokenType().equals(TokenType.EXPLORE)) {
                recvExplore(token);
            }
            else {
                recvReject(token.getSenderID());
            }
        }
    }

    private void recvAnouncement(int leaderID) {
        assert maxUID <= leaderID;
        maxUID = leaderID;
        status = Status.NONLEADER;
        StringBuilder sb = new StringBuilder();
        sb.append("Thread ").append(UID).append(": LeaderID = ").append(maxUID).append(". Message sent = ").append(msgSent);
        System.out.println(sb.toString());
    }

    private void recvExplore(Token token) throws InterruptedException, ThreadException {
        if (token.getMaxID() > UID) {
            UID = token.getMaxID();
            parentUID = token.getSenderID();
            sendExplore();
        }
        else if (token.getMaxID() < UID) {
            sendReject(token.getSenderID());
        }
        else
            throw new ThreadException(ErrorCode.RECEIVING_OWN_UID_FROM_NEIGHBOR);
    }

    private void recvComplete(int senderID) {

    }

    private void recvReject(int senderID) {

    }

    private void sendAnouncement() {

    }

    private void sendExplore() throws InterruptedException {
        for (Integer neighbor: sendChannels.keySet()) {
            if (!neighbor.equals(parentUID)) {
                Thread.sleep(delay());
                Token exploreToken = new Token(UID, maxUID, TokenType.EXPLORE, round);
                sendChannels.get(neighbor).put(exploreToken);
                msgSent ++;
            }
        }
    }

    private void sendReject (int senderID) throws InterruptedException {
        Thread.sleep(delay());
        Token rejectToken = new Token(UID, maxUID, TokenType.REJECT, round);
        sendChannels.get(senderID).put(rejectToken);
        msgSent ++;
    }

    private void sendComplete () {

    }

    private void sendDummy (int neighborUID) throws InterruptedException {
        Thread.sleep(delay());
        Token dummyToken = new Token(UID, maxUID, TokenType.DUMMY, round);
        sendChannels.get(neighborUID).put(dummyToken);
    }

    private int delay() {;
        return new Random().nextInt(10)+1;
    }

    int getUID () {
        return UID;
    }

}