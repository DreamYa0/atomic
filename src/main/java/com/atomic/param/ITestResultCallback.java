package com.atomic.param;

import java.util.Map;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 函数式接口
 * @Data 2018/05/30 10:48
 */
@FunctionalInterface
public interface ITestResultCallback {

    /**
     * 方法执行后回调
     * @param context      接口入参map对象
     * @param result     接口返回结果
     * @param parameters 接口入参对象
     * @throws Exception
     */
    void afterTestMethod(Map<String, Object> context, Object result, Object... parameters) throws Exception;
}
