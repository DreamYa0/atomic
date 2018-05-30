package com.atomic.exception;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 通用反射调用异常类
 * @Data 2018/05/30 10:48
 */
public class InvokeException extends RuntimeException {

    private String message;

    public InvokeException(String message) {
        this.message = message;
    }

    public InvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvokeException(Throwable cause) {
        super(cause);
    }

    protected InvokeException(String message, Throwable cause,
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
