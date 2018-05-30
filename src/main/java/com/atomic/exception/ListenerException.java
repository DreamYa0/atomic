package com.atomic.exception;

/**
 * Created by dreamyao on 2017/6/7.
 */
public class ListenerException extends RuntimeException {

    private String message;

    public ListenerException(String message) {
        this.message = message;
    }

    public ListenerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ListenerException(Throwable cause) {
        super(cause);
    }

    protected ListenerException(String message, Throwable cause,
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
