package com.atomic.tools.assertcheck.enums;

/**
 * 断言方式，旧值是执行接口前的数据库值，还是执行接口后的数据库值
 * @author dreamyao
 * @title xxx
 * @date 2018/05/30 10:48
 * @since v1.0.0
 */
public enum AssertType {

    /**
     * 旧值是执行接口前的数据库值
     */
    OLD_VALUE_BEFORE_TEST(0),
    /**
     * 旧值是执行接口后的数据库值
     */
    OLD_VALUE_AFTER_TEST(1);

    private int code;

    AssertType(int code) {
        this.code = code;
    }

    public static AssertType getAssertType(int code) {
        for (AssertType assertType : AssertType.values()) {
            if (assertType.getCode() == code) {
                return assertType;
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
