package com.atomic.util;

import com.alibaba.fastjson.JSON;
import com.atomic.param.Constants;
import com.atomic.param.entity.QaScenarioData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.testng.IClass;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.util.Date;
import java.util.List;
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
     * 保存测试数据，为测试数据入库做准备
     * @param context
     * @param result
     */
    @Deprecated
    public static void resultIntoDb(Object[] parameters, Map<String, Object> context, Object result) {
        Map<String, Object> param = Maps.newHashMap();
        param.putAll(context);
        Object caseName = param.get(Constants.CASE_NAME);
        Object expectedReturn = param.get(Constants.EXPECTED_RESULT);
        if (!"".equals(caseName) && caseName != null) {
            Reporter.log(caseName.toString());
            Reporter.log(JSON.toJSONString(parameters[0]));//记录接口入参
            Reporter.log(JSON.toJSONString(result));//记录接口返回结果
        }
        if (expectedReturn == null || "".equals(expectedReturn)) {
            Reporter.log(null);
        } else {
            Reporter.log(expectedReturn.toString());
        }
    }

    /**
     * Http接口测试保存测试数据，为测试数据入库做准备
     * @param parameters
     * @param result
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static void resultIntoDb(ITestResult iTestResult, Map<String, Object> parameters, Object result) {
        Map<String, Object> context = (Map<String, Object>) iTestResult.getParameters()[0];
        // 接口测试案例名称
        String caseName = context.get("caseName").toString();
        Object expectedReturn = context.get(Constants.EXPECTED_RESULT);
        if (!"".equals(caseName) && caseName != null) {
            Reporter.log(caseName);
            Reporter.log(parameters.toString());//记录接口入参
            Reporter.log(result.toString());//记录接口返回结果
        }
        if (expectedReturn == null || "".equals(expectedReturn)) {
            Reporter.log(null);
        } else {
            Reporter.log(expectedReturn.toString());
        }
    }

    /**
     * 移除不需要的值
     * @param param
     * @param keys
     */
    public static void removeMapValue(Map<String, Object> param, List<String> keys) {
        for (String key : keys) {
            param.remove(key);
        }
    }

    /**
     * 保存测试场景相关数据-Dubbo
     * @param parameters 测试方法入参
     * @param result     测试方法返回结果
     * @param testResult 测试结果视图
     * @param context    入参上下文
     */
    public static void saveScenarioTestData(Object[] parameters, Object result, ITestResult testResult, Map<String, Object> context, String... className) {
        String name;
        if (testResult != null) {
            name = testResult.getMethod().getMethodName();
        } else {
            name = className[0];
        }
        Gson gson = new Gson();
        String jsonParam;
        try {
            jsonParam = gson.toJson(parameters[0]);
        } catch (Exception e) {
            jsonParam = JSON.toJSONString(parameters[0]);
        }
        String deleteSql = "delete from qa_scenario_data where method_name=? and method_parameter=?";
        Object[] deleteParams = {name, jsonParam};
        CIDbUtils.updateValue(deleteSql, deleteParams);
        String insertValue = "insert into qa_scenario_data (method_name,method_parameter,method_return,case_index,create_time) values (?,?,?,?,?)";
        Object[] insertParams;
        if (parameters[0] != null || "".equals(parameters[0])) {
            insertParams = new Object[]{name, jsonParam, gson.toJson(result), context.get(CASE_INDEX), new Date()};
        } else {
            insertParams = new Object[]{name, null, gson.toJson(result), context.get(CASE_INDEX), new Date()};
        }
        CIDbUtils.updateValue(insertValue, insertParams);
    }

    /**
     * 保存测试场景相关数据-Http
     * @param parameters 测试方法入参
     * @param result     测试方法返回结果
     * @param testResult 测试结果视图
     * @param context    入参上下文
     */
    public static void saveScenarioTestData(Map<String, Object> parameters, Object result, ITestResult testResult, Map<String, Object> context, String... className) {
        String name;
        if (testResult != null) {
            name = testResult.getMethod().getMethodName();
        } else {
            name = className[0];
        }
        String deleteSql = "delete from qa_scenario_data where method_name=? and method_parameter=?";
        Object[] deleteParams = {name, parameters.toString()};
        CIDbUtils.updateValue(deleteSql, deleteParams);
        String insertValue = "insert into qa_scenario_data (method_name,method_parameter,method_return,case_index,create_time) values (?,?,?,?,?)";
        Object[] insertParams;
        if (parameters.keySet().size() > 0) {
            insertParams = new Object[]{name, parameters.toString(), result.toString(), context.get(CASE_INDEX), new Date()};
        } else {
            insertParams = new Object[]{name, null, result.toString(), context.get(CASE_INDEX), new Date()};
        }
        CIDbUtils.updateValue(insertValue, insertParams);
    }

    /**
     * 根据测试方法名称获取测试结果
     * @param className 测试类名
     * @param caseIndex 测试用例序号
     * @return 方法返回结果
     */
    public static String getScenarioTestData(String className, Object caseIndex) {
        String querySql = "select * from qa_scenario_data where method_name=? and case_index=? order by create_time desc";
        Object[] queryParams = {className, caseIndex};
        QaScenarioData scenarioData = CIDbUtils.queryQaScenarioTestValue(querySql, queryParams);
        if (scenarioData != null) {
            return scenarioData.getMethod_return();
        }
        return null;
    }

    /**
     * 把方法返回值保存到本地缓存中-Dubbo、Http
     * @param result     方法返回结果
     * @param testResult 测试结果视图
     * @param context    入参上下文
     */
    public static void saveTestResultInCache(Object result, ITestResult testResult, Map<String, Object> context) {
        String name = getTestCaseClassName(testResult) + "_" + context.get(CASE_INDEX);
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
        String name = getTestCaseClassName(testResult) + "_" + context.get(CASE_INDEX);
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

    /**
     * 获取测试用例的Class名称
     * @param testResult 测试结果上下文
     * @return
     */
    private static String getTestCaseClassName(ITestResult testResult) {
        IClass iClass = testResult.getTestClass();
        String testClassName = iClass.getName();
        String[] names = testClassName.split("\\.");
        return names[names.length - 1];
    }
}
