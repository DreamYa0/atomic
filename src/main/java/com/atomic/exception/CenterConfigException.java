package com.atomic.exception;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2018/05/30 10:48
 */
public class CenterConfigException extends RuntimeException {

    private String message;

    public CenterConfigException(String message) {
        this.message = message;
    }

    public CenterConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public CenterConfigException(Throwable cause) {
        super(cause);
    }

    protected CenterConfigException(String message, Throwable cause,
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
