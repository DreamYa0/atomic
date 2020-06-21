package com.atomic.tools.assertor;

import org.testng.ITestResult;

/**
 * @author dreamyao
 * @title 通用断言接口
 * @Data 2018/5/19 下午8:47
 * @since 1.0.0
 */
public interface Assertor {

    /**
     * 结果断言
     * @param result     返回结果
     * @param testResult 测试上下文
     * @param instance   测试类实列
     */
    void assertResult(ITestResult testResult, Object result, Object instance);

}
