package com.atomic.exception;

/**
 * @author dreamyao
 * @version 1.0.0
 * @description
 * @Data 2018/05/30 10:48
 */
public class MongoDbException extends RuntimeException {

    private String message;

    public MongoDbException(String message) {
        this.message = message;
    }

    public MongoDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public MongoDbException(Throwable cause) {
        super(cause);
    }

    protected MongoDbException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
