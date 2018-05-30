package com.atomic.exception;

/**
 * 通用数据回滚异常类
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/5/31.
 */
public class RollBackException extends RuntimeException {

    private String message;

    public RollBackException(String message) {
        this.message = message;
    }

    public RollBackException(String message, Throwable cause) {
        super(message, cause);
    }

    public RollBackException(Throwable cause) {
        super(cause);
    }

    protected RollBackException(String message, Throwable cause,
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
