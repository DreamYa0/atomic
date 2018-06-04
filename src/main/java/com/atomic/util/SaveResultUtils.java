package com.atomic.util;

import com.atomic.param.TestNGUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.testng.ITestResult;

import java.util.Map;

import static com.atomic.param.Constants.CASE_INDEX;


/**
 * 通用数据保存工具类
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/5/29.
 */
public final class SaveResultUtils {

    private static Cache<String, Object> testDataCache = CacheBuilder.newBuilder().build();
    private static Cache<String, Object> testRequestCache = CacheBuilder.newBuilder().build();

    private SaveResultUtils() {
    }

    /**
     * 把方法返回值保存到本地缓存中-Dubbo、Http
     * @param result     方法返回结果
     * @param testResult 测试结果视图
     * @param context    入参上下文
     */
    public static void saveTestResultInCache(Object result, ITestResult testResult, Map<String, Object> context) {
        String name = TestNGUtils.getTestCaseClassName(testResult) + "_" + context.get(CASE_INDEX);
        testDataCache.put(name, result);
    }

    /**
     * 从本地缓存中获取方法返回结果-Dubbo、Http
     * @param className 测试类名
     * @param caseIndex 测试用例序号
     * @return 方法返回结果
     */
    public static Object getTestResultInCache(String className, Object caseIndex) {
        String cacheKey = className + "_" + caseIndex;
        return testDataCache.getIfPresent(cacheKey);
    }

    public static void saveTestRequestInCache(Object parameters, ITestResult testResult, Map<String, Object> context) {
        String name = TestNGUtils.getTestCaseClassName(testResult) + "_" + context.get(CASE_INDEX);
        testRequestCache.put(name, parameters);
    }

    /**
     * 在场景测试时可以从本地缓存中获取接口入参对象
     * @param className 测试类名
     * @param caseIndex 测试用例序号
     * @return 接口入参对象
     */
    public static Object getTestRequestInCahe(String className, Object caseIndex) {
        String cacheKey = className + "_" + caseIndex;
        return testRequestCache.getIfPresent(cacheKey);
    }
}
