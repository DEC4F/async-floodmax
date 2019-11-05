package edu.utdalas.cs6380;

/**
 * General exception class for this environment
 *
 * Created by Stan Tian on 09/17/2019.
 */
class ThreadException extends Exception {

    private static final long serialVersionUID = -4974836832746069995L;

    public enum ErrorCode {
        UNEXPECTED_INDEX
    }

    ErrorCode errorCode;

    ThreadException(String message, Throwable e){
        super(message, e);
    }

    ThreadException(String message) {
        super(message);
    }

    ThreadException(ErrorCode errorCode){
        this.errorCode = errorCode;
    }
}
