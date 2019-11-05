package edu.utdalas.cs6380;

/***
 * Class representing the token transmitted between processes
 */
final class Token {

    //////////////////////////////////
    // ENUM
    //////////////////////////////////

    enum TokenType {
        Election,
        Anouncement
    }

    //////////////////////////////////
    // FIELDS
    //////////////////////////////////

    private int senderID;
    private TokenType type;

    //////////////////////////////////
    // CONSTRUCTOR
    //////////////////////////////////

    Token(int senderID, TokenType type) {
        this.senderID = senderID;
        this.type = type;
    }

    //////////////////////////////////
    // ACCESSORS
    //////////////////////////////////

    int getSenderID() {
        return senderID;
    }

    TokenType getTokenType() {
        return type;
    }

}