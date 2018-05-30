package com.atomic.enums;

/**
 * Http接口响应消息返回类型
 * @author yangminhan
 * @version 1.0
 *          Created by yangminhan on 2017/6/18.
 */
public enum CheckMessage {
    NORM(1, "返回String"),
    SPEC(2, "返回Entity");

    private int code;
    private String text;

    CheckMessage(int code, String desp) {
        this.code = code;
        this.text = desp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
