package com.atomic.exception;

/**
 * 工具类异常
 * @author dreamyao
 */
public class UtilException extends RuntimeException {
    private String message;

    public UtilException(String message) {
        this.message = message;
    }

    public UtilException(String message, Throwable cause) {
        super(message, cause);
    }

    public UtilException(Throwable cause) {
        super(cause);
    }

    protected UtilException(String message, Throwable cause,
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
