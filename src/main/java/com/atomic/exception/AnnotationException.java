package com.atomic.exception;

/**
 * 通用注解异常类
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/5/28.
 */
public class AnnotationException extends RuntimeException {

    private String message;

    public AnnotationException(String message) {
        this.message = message;
    }

    public AnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnnotationException(Throwable cause) {
        super(cause);
    }

    protected AnnotationException(String message, Throwable cause,
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
