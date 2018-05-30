package com.atomic.config;


public enum GlobalMode {

    NORMAL(1, "正常模式"),
    REPLAY(2, "重放模式");

    private int code;

    private String text;

    GlobalMode(int code, String text) {
        this.code = code;
        this.text = text;
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
