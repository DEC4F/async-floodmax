package edu.utdalas.cs6380;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.Random;

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
    private int round;
    private Status status;
    private int numReply;
    private int msgSent;
    private AsyncThread parent;
    private Map<AsyncThread, BlockingQueue<Token>> sendChannels; // <neighbor, channel>
    private Map<AsyncThread, BlockingQueue<Token>> recvChannels;
    private Map<AsyncThread, Token> recvMsg; // <neighbor, token>
    private Map<AsyncThread, Boolean> hasTalkedTo; // <neighbor, hasTalked>

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
        hasTalkedTo = new HashMap<>();
    }

    //////////////////////////////////
    // INTERFACE
    //////////////////////////////////

    @Override
    public void run() {
        try {
            // init
            flood(new Token(UID, maxUID, TokenType.EXPLORE, round));
            System.out.println("Thread!!!" + Integer.toString(UID));
            while (status.equals(Status.UNKNOWN)) {
                // reset talkedTo for this "round"
                for (AsyncThread neighbor : sendChannels.keySet()) {
                    hasTalkedTo.put(neighbor, false);
                }
                recv();
                round ++;
                System.out.println("Round = " + round);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addNeighbor(AsyncThread neighbor, BlockingQueue<Token> sendChan, BlockingQueue<Token> recvChan) {
        sendChannels.put(neighbor, sendChan);
        recvChannels.put(neighbor, recvChan);
        numReply ++;
    }
    
    //////////////////////////////////
    // HELPERS
    //////////////////////////////////

    /**
     * receive tokens from channels
     */
    private void recv() throws InterruptedException, ThreadException {
        recvMsg = new HashMap<>();
        for (AsyncThread neighbor : recvChannels.keySet()) {
            BlockingQueue<Token> chann = recvChannels.get(neighbor);
            while (!chann.isEmpty() && chann.peek().getRoundTag() <= round) {
                recvMsg.put(neighbor, chann.take());
                System.out.println("Thread " + Integer.toString(UID) + ": i'm stuck here");
            }
            System.out.println("Thread " + Integer.toString(UID) + ": wait, im not");
        }
        parseRecvToken();
    }

    /***
     * read all received msg in this "round"
     * @throws InterruptedException
     * @throws ThreadException
     */
    private void parseRecvToken () throws InterruptedException, ThreadException {
        if (recvAnouncement()) {return;}
        recvExplore();
        recvReject();
        recvCompleted();
        sendDummy();
    }

    /**
     * update status and leaderID, flood neighbors, print leader ID
     * @param leaderID
     * @throws InterruptedException
     */
    private Boolean recvAnouncement() throws InterruptedException {
        for (AsyncThread neighbor : recvMsg.keySet()) {
            Token token = recvMsg.get(neighbor);
            if (token.getTokenType().equals(TokenType.ANOUNCEMENT) && status.equals(Status.UNKNOWN)) {
                // flood anouncement token
                maxUID = token.getMaxID();
                status = Status.NONLEADER;
                flood(new Token(UID, maxUID, TokenType.ANOUNCEMENT, round));
                // print leader info
                StringBuilder sb = new StringBuilder();
                sb.append("Thread ").append(UID).append(": LeaderID = ").append(maxUID).append(". Message sent = ").append(msgSent);
                System.out.println(sb.toString());
                return true;
            }
        }
        return false;
    }

    /**
     * update maxUID, flood neighbors if token has greater maxUID
     * reply reject if token is smaller
     * @throws InterruptedException
     * @throws ThreadException
     */
    private void recvExplore() throws InterruptedException, ThreadException {
        Boolean hasNewInfo = false;
        for (AsyncThread neighbor : recvMsg.keySet()) {
            Token token = recvMsg.get(neighbor);
            if (token.getTokenType().equals(TokenType.EXPLORE)) {
                if (token.getMaxID() > maxUID) {
                    hasNewInfo = true;
                    maxUID = token.getMaxID();
                    parent = neighbor;
                    // update variable if parent points to a neighbor
                    if (numReply == recvChannels.size())
                        numReply --;
                }
                else {
                    send(new Token(UID, maxUID, TokenType.REJECT, round), neighbor);
                }
            }
        }
        if (hasNewInfo) {
            flood(new Token(UID, maxUID, TokenType.EXPLORE, round));
        }
    }

    /**
     * 
     * @throws InterruptedException
     */
    private void recvReject() throws InterruptedException {
        for (AsyncThread neighbor : recvMsg.keySet()) {
            Token token = recvMsg.get(neighbor);
            if (token.getTokenType().equals(TokenType.REJECT)) {
                numReply --;
            }
        }
        if (numReply == 0)
            send(new Token(UID, maxUID, TokenType.COMPLETED, round), parent);
    }

    /**
     * 
     * @throws InterruptedException
     */
    private void recvCompleted() throws InterruptedException {
        for (AsyncThread neighbor : recvMsg.keySet()) {
            Token token = recvMsg.get(neighbor);
            if (token.getTokenType().equals(TokenType.COMPLETED)) {
                numReply --;
            }
        }
        if (numReply == 0) {
            // all children have completed, forward to parent
            if (parent != this)
                send(new Token(UID, maxUID, TokenType.COMPLETED, round), parent);
            // leader is found
            else {
                flood(new Token(UID, UID, TokenType.ANOUNCEMENT, round));
                MasterThread.leaderID = UID;
            }
        }
    }

    /**
     * 
     * @throws InterruptedException
     */
    private void sendDummy() throws InterruptedException {
        for (AsyncThread neighbor : hasTalkedTo.keySet()) {
            if (!hasTalkedTo.get(neighbor))
                send(new Token(UID, maxUID, TokenType.DUMMY, round), neighbor);
        }
    }

    /**
     * flood non-parent neighbors with input token, increment message sent
     * @param token
     * @throws InterruptedException
     */
    private void flood(Token token) throws InterruptedException {
        for (AsyncThread neighbor: sendChannels.keySet()) {
            if (!neighbor.equals(parent)) {
                Thread.sleep(delay());
                sendChannels.get(neighbor).put(token);
                hasTalkedTo.put(neighbor, true);
                msgSent ++;
            }
        }
    }

    /**
     * send reject to smaller token sender
     * @param senderID
     * @throws InterruptedException
     */
    private void send(Token token, AsyncThread neighbor) throws InterruptedException {
        Thread.sleep(delay());
        sendChannels.get(neighbor).put(token);
        hasTalkedTo.put(neighbor, true);
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