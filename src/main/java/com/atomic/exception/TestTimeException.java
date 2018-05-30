package com.atomic.exception;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 通用测试时间相关异常类
 * @Data 2017/7/30 17:35
 */
public class TestTimeException extends RuntimeException {

    private String message;

    public TestTimeException(String message) {
        this.message = message;
    }

    public TestTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestTimeException(Throwable cause) {
        super(cause);
    }

    protected TestTimeException(String message, Throwable cause,
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
