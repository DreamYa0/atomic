package com.atomic.param;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 函数式接口
 * @Data 2017-8-24
 */
@FunctionalInterface
public interface ITestMethodMultiTimes {

    /**
     * 执行测试函数
     * @param paramValue 遍历值或者传入值
     */
    void execTestMethod(Object paramValue) throws Exception;
}
