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
    private int msgSent;
    private int numReply = 0;
    private boolean finished;
    private AsyncThread parent = this;
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
        finished = false;
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
            // init stage
            flood(new Token(UID, maxUID, TokenType.EXPLORE, round));
            // intermediate stage
            while (status.equals(Status.UNKNOWN)) {
                resetHasTalkedTo();
                recv();
                round ++;
            }
            // final stage
            printLeaderInfo();
            finished = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * set up in and out channels between current process and neighbor
     * @param neighbor is the neighboring proc
     * @param sendChan put out msg here
     * @param recvChan recv in msg here
     */
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
     * @throws InterruptedException
     */
    private void recv() throws InterruptedException {
        recvMsg = new HashMap<>();
        for (AsyncThread neighbor : recvChannels.keySet()) {
            BlockingQueue<Token> chann = recvChannels.get(neighbor);
            while (!chann.isEmpty() && chann.peek().getRoundTag() <= round) {
                recvMsg.put(neighbor, chann.take());
            }
        }
        parseRecvToken();
    }

    /***
     * read all received msg in this "round"
     * @throws InterruptedException
     */
    private void parseRecvToken () throws InterruptedException {
        if (recvAnouncement()) {return;}
        recvExplore();
        recvReject();
        recvCompleted();
        sendDummy();
    }

    /**
     * update status and leaderID, flood neighbors
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
                return true;
            }
        }
        return false;
    }

    /**
     * update maxUID, flood neighbors if token has greater maxUID
     * reply reject if token is smaller
     * @throws InterruptedException
     */
    private void recvExplore() throws InterruptedException {
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
     * calculate number of rejected neighbors and send completed token to parent
     * if all neighbors are either rejected or completed
     * @throws InterruptedException
     */
    private void recvReject() throws InterruptedException {
        for (AsyncThread neighbor : recvMsg.keySet()) {
            Token token = recvMsg.get(neighbor);
            if (token.getTokenType().equals(TokenType.REJECT)) {
                numReply --;
            }
        }
        if (numReply == 0 && parent != this) {
            send(new Token(UID, maxUID, TokenType.COMPLETED, round), parent);
        }
    }

    /**
     * calculate number of completed token received
     * if all children have completed, then send completed token to parent
     * if parent is this proc itself, then this is the leader, send anouncement token
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
                status = Status.LEADER;
                flood(new Token(UID, UID, TokenType.ANOUNCEMENT, round));
                MasterThread.leaderID = UID;
            }
        }
    }

    /**
     * send dummy token to neighbor(s) if this proc hasn't talked to it in this round
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
     * send a token to a neighbor
     * @param token the token to be sent
     * @param neighbor the recipient of the token
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
     * @return a random delay between 1 to 10 ms
     */
    private int delay() {;
        return new Random().nextInt(10)+1;
    }

    /**
     * resets hasTalkedTo so that neighbors that 
     * this process hasn't send message to in this round can be flagged
     */
    private void resetHasTalkedTo() {
        for (AsyncThread neighbor : sendChannels.keySet()) {
            hasTalkedTo.put(neighbor, false);
        }
    }

    /**
     * prints leader information and message sent to stdout
     */
    private void printLeaderInfo() {
        // print leader info
        StringBuilder sb = new StringBuilder();
        sb.append("Thread ").append(UID).append(": LeaderID = ").append(maxUID).append(". Message sent = ").append(msgSent);
        System.out.println(sb.toString());
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

    boolean getFinished() {
        return finished;
    }

}