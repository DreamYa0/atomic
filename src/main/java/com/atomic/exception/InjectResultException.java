package com.atomic.exception;

/**
 * 通用测试方法注入入参和返回值异常
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/6/17.
 */
public class InjectResultException extends RuntimeException {

    private String message;

    public InjectResultException(String message) {
        this.message = message;
    }

    public InjectResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public InjectResultException(Throwable cause) {
        super(cause);
    }

    protected InjectResultException(String message, Throwable cause,
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
