package com.atomic.exception;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 通用数据库连接异常
 * @Data 2018/05/30 10:48
 */
public class DatabaseException extends RuntimeException {

    private String message;

    public DatabaseException(String message) {
        this.message = message;
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }

    protected DatabaseException(String message, Throwable cause,
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
