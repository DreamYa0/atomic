package com.atomic.tools.mock.data;

public enum TestMethodMode {
    NORMAL(1, "正常模式"),
    REC(2, "录制模式"),
    REPLAY(3, "重放模式");


    private int code;

    private String text;

    TestMethodMode(int code, String text) {
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
