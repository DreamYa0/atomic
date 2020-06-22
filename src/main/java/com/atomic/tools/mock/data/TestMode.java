package com.atomic.tools.mock.data;

public enum TestMode {

    // 开发环境
    DEV("dev"),
    // 测试环境1
    T1("t1"),
    // 测试环境2
    T2("t2"),
    // 测试环境3
    T3("t3"),
    // 测试环境4
    T4("t4"),
    // demo 环境
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
