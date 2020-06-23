package com.atomic.exception;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date  2017/7/30 22:20
 */
public class UserKeyException extends RuntimeException {

    private String message;

    public UserKeyException(String message) {
        this.message = message;
    }

    public UserKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserKeyException(Throwable cause) {
        super(cause);
    }

    protected UserKeyException(String message, Throwable cause,
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
