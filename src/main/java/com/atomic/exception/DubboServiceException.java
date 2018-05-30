package com.atomic.exception;

/**
 * 通用dubbo服务调用异常
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/6/17.
 */
public class DubboServiceException extends RuntimeException {

    private String message;

    public DubboServiceException(String message) {
        this.message = message;
    }

    public DubboServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DubboServiceException(Throwable cause) {
        super(cause);
    }

    protected DubboServiceException(String message, Throwable cause,
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
