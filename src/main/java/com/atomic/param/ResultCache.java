package com.atomic.param;

import com.atomic.util.TestNGUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.testng.ITestResult;

import java.util.Map;
import java.util.Objects;

import static com.atomic.param.Constants.CASE_INDEX;


/**
 * 通用数据保存工具类
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/5/29.
 */
public final class ResultCache {

    private static final Cache<String, Object> TEST_DATA_CACHE = CacheBuilder.newBuilder().build();
    private static final Cache<String, Object> TEST_REQUEST_CACHE = CacheBuilder.newBuilder().build();

    private ResultCache() {
    }

    public static void saveTestResultInCache(Object result, ITestResult testResult, Map<String, Object> context) {
        // 把方法返回值保存到本地缓存中-Dubbo、Http
        if (Objects.nonNull(result)) {
            String name = TestNGUtils.getTestCaseClassName(testResult) + "_" + context.get(CASE_INDEX);
            TEST_DATA_CACHE.put(name, result);
        }
    }

    public static Object getTestResultInCache(String className, Object caseIndex) {
        // 从本地缓存中获取方法返回结果-Dubbo、Http
        String cacheKey = className + "_" + caseIndex;
        return TEST_DATA_CACHE.getIfPresent(cacheKey);
    }

    public static void saveTestRequestInCache(Object parameters,
                                              ITestResult testResult,
                                              Map<String, Object> context) {

        if (Objects.nonNull(parameters)) {
            String name = TestNGUtils.getTestCaseClassName(testResult) + "_" + context.get(CASE_INDEX);
            TEST_REQUEST_CACHE.put(name, parameters);
        }
    }

    public static Object getTestRequestInCache(String className, Object caseIndex) {
        // 在场景测试时可以从本地缓存中获取接口入参对象
        String cacheKey = className + "_" + caseIndex;
        return TEST_REQUEST_CACHE.getIfPresent(cacheKey);
    }
}
