package com.atomic.exception;

/**
 * 通用智能化断言异常类
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/6/18.
 */
public class AssertCheckException extends RuntimeException {

    private String message;

    public AssertCheckException(String message) {
        this.message = message;
    }

    public AssertCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssertCheckException(Throwable cause) {
        super(cause);
    }

    protected AssertCheckException(String message, Throwable cause,
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
