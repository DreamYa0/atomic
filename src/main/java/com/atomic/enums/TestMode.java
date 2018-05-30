package com.atomic.enums;

public enum TestMode {

    // 测试环境1
    TEST_ONE("testOne"),
    // 测试环境2
    TEST_TWO("testTwo"),
    // 测试环境3
    TEST_THREE("testThree"),
    //
    TESTING("testing")

    ;


    private String name;

    TestMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
