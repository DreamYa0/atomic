package com.atomic.tools.autotest;

public enum AutoTestMode {
    /**
     * 跳过自动化测试用例
     */
    NONE(0),
    /**
     * 多属性模式，属性组合排列
     */
    MULTIPLE(1),
    /**
     * 单属性模式，每次一个属性变化，其他属性使用正常值
     */
    SINGLE(2);

    private int mode;

    AutoTestMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
