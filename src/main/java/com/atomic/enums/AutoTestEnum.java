package com.atomic.enums;

public enum AutoTestEnum {

    NORMAL(2),
    SMALL(1),
    VERY_SMALL(0);

    private int level;

    AutoTestEnum(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
