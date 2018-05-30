package com.atomic.exception;

/**
 * Created by dreamyao on 2017/6/23.
 */
public class ScenarioDubboException extends RuntimeException {

    private String message;

    public ScenarioDubboException(String message) {
        this.message = message;
    }

    public ScenarioDubboException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScenarioDubboException(Throwable cause) {
        super(cause);
    }

    protected ScenarioDubboException(String message, Throwable cause,
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
