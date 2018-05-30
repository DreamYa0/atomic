package com.atomic.exception;

/**
 * 通用测试属性获取异常
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/6/22.
 */
public class MethodMetaException extends RuntimeException {

    private String message;

    public MethodMetaException(String message) {
        this.message = message;
    }

    public MethodMetaException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodMetaException(Throwable cause) {
        super(cause);
    }

    protected MethodMetaException(String message, Throwable cause,
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
