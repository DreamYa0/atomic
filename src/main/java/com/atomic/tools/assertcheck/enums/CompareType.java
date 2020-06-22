package com.atomic.tools.assertcheck.enums;

/**
 * 断言项比较方式
 * @title xxx
 * @since v1.0.0
 */
public enum CompareType {

    /**
     * 等于
     */
    EQUALS(0),
    /**
     * 大于
     */
    GREATER_THAN(1),
    /**
     * 小于
     */
    LESS_THAN(2),
    /**
     * 小于等于
     */
    LESS_THAN_AND_EQUALS(3),
    /**
     * 大于等于
     */
    GREATER_THAN_AND_EQUALS(4),
    /**
     * 大于某个具体值
     */
    GREATER_THAN_VALUE(5),
    /**
     * 小于某个具体指
     */
    LESS_THAN_VALUE(6),
    /**
     * 同一天
     */
    SAME_DAY(7),
    /**
     * 今天
     */
    TODAY(8),
    /**
     * 时间戳是同一天
     */
    SAME_DAY_UNIX_TIME(9),
    /**
     * 时间戳是今天
     */
    TODAY_UNIX_TIME(10),
    /**
     * 不等于
     */
    NOT_EQUALS(11);

    private int code;

    CompareType(int code) {
        this.code = code;
    }

    public static CompareType getCompareType(int code) {
        for (CompareType compareType : CompareType.values()) {
            if (compareType.getCode() == code) {
                return compareType;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
