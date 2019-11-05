package edu.utdalas.cs6380;

/***
 * Class representing the token transmitted between processes
 */
final class Token {

    //////////////////////////////////
    // ENUM
    //////////////////////////////////

    enum TokenType {
        ANOUNCEMENT,
        DUMMY,
        COMPLETED,
        EXPLORE,
        REJECT
    }

    //////////////////////////////////
    // FIELDS
    //////////////////////////////////

    private int senderID;
    private int maxID;
    private TokenType type;
    private int roundTag;

    //////////////////////////////////
    // CONSTRUCTOR
    //////////////////////////////////

    Token(int senderID, int maxID, TokenType type, int roundTag) {
        this.senderID = senderID;
        this.maxID = maxID;
        this.type = type;
        this.roundTag = roundTag;
    }

    //////////////////////////////////
    // ACCESSORS
    //////////////////////////////////

    int getSenderID() {
        return senderID;
    }

    int getMaxID() {
        return maxID;
    }

    TokenType getTokenType() {
        return type;
    }

    int getRoundTag() {
        return roundTag;
    }

}