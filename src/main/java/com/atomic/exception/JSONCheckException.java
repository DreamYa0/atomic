package com.atomic.exception;

/**
 * JSON对象操作异常
 * @author Yangminhan
 * @version 1.0
 *          <p>
 *          Created by Yangminhan on 2017/6/15.
 */
public class JSONCheckException extends RuntimeException {

    public JSONCheckException() {
        super();
    }

    public JSONCheckException(String message) {
        super(message);
    }
}
