package com.atomic.exception;

/**
 * 通用自动化测试异常
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/6/17.
 */
public class AutoTestException extends RuntimeException {

    private String message;

    public AutoTestException(String message) {
        this.message = message;
    }

    public AutoTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public AutoTestException(Throwable cause) {
        super(cause);
    }

    protected AutoTestException(String message, Throwable cause,
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
