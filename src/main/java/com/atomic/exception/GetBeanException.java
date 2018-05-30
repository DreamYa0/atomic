package com.atomic.exception;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2017/8/30 13:56
 */
public class GetBeanException extends RuntimeException {

    private String message;

    public GetBeanException(String message) {
        this.message = message;
    }

    public GetBeanException(String message, Throwable cause) {
        super(message, cause);
    }

    public GetBeanException(Throwable cause) {
        super(cause);
    }

    protected GetBeanException(String message, Throwable cause,
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
