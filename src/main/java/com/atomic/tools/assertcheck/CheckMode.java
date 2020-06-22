package com.atomic.tools.assertcheck;

/**
 * 智能化断言模式
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/6/18.
 */
public enum CheckMode {

    NORMAL(1, "正常模式"),
    REC(2, "录制模式"),
    REPLAY(3, "重放模式");

    private int code;

    private String text;

    CheckMode(int code, String text) {
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
