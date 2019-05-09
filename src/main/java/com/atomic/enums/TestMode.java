package com.atomic.enums;

public enum TestMode {

    // 测试环境1
    DEV("dev"),
    // 测试环境2
    TEST("test"),
    // 测试环境3
    DEMO("demo"),

    ;


    private String name;

    TestMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
