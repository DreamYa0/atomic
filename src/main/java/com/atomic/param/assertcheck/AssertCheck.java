package com.atomic.param.assertcheck;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atomic.exception.AssertCheckException;
import com.atomic.param.Constants;
import com.atomic.param.ITestResultCallback;
import com.atomic.param.ResultAssert;
import com.atomic.param.entity.MethodMeta;
import com.atomic.param.entity.QaAutoAssert;
import com.atomic.util.CIDbUtils;
import org.testng.Assert;
import org.testng.Reporter;

import java.util.Date;
import java.util.Map;

/**
 * 通用智能化断言工具类
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/6/18.
 */
public final class AssertCheck {

    private AssertCheck() {
    }

    /**
     * 录制智能化断言相关数据 - Dubbo接口
     * @param parameter  测试入参
     * @param result     测试返回结果
     * @param methodMeta 测试相关属性
     */
    public static void recMode(Object parameter, Object result, MethodMeta methodMeta) {
        String querySql = "select * from qa_auto_assert where method_name=? and method_param=?";
        Object[] queryParam = {methodMeta.getMethodName(), JSON.toJSONString(parameter)};
        QaAutoAssert autoAssert = CIDbUtils.queryQaAutoAssetValue(querySql, queryParam);
        if (autoAssert == null) {
            insertAssertValue(methodMeta.getMethodName(), parameter, result);
        } else {
            String deleteSql = "delete from qa_auto_assert where method_name=? and method_param=?";
            CIDbUtils.updateValue(deleteSql, queryParam);//删除存在的数据，从新插入新的数据
            insertAssertValue(methodMeta.getMethodName(), parameter, result);
        }
    }

    /**
     * 录制智能化断言相关数据 - Http接口
     * @param parameter  测试入参
     * @param result     测试返回结果
     * @param methodName 测试相关属性
     */
    public static void recMode(Object parameter, String result, String methodName) {
        String querySql = "select * from qa_auto_assert where method_name=? and method_param=?";
        Object[] queryParam = {methodName, parameter.toString()};
        QaAutoAssert autoAssert = CIDbUtils.queryQaAutoAssetValue(querySql, queryParam);
        if (autoAssert == null) {
            insertAssertValue(methodName, parameter, result);
        } else {
            String deleteSql = "delete from qa_auto_assert where method_name=? and method_param=?";
            CIDbUtils.updateValue(deleteSql, queryParam);//删除存在的数据，从新插入新的数据
            insertAssertValue(methodName, parameter, result);
        }
    }

    /**
     * 将录制断言对应的接口测试类名,入参,返回结果写入数据库
     * @param methodName
     * @param parameter
     * @param result
     */
    private static void insertAssertValue(String methodName, Object parameter, String result) {
        String insertSql = "insert into qa_auto_assert (method_name,method_param,method_return,create_time) values(?,?,?,?)";
        Object[] insertParam = {methodName, parameter.toString(), result, new Date()};
        CIDbUtils.updateValue(insertSql, insertParam);
    }

    /**
     * 执行录制断言与实际结果比较 - Dubbo接口
     * @param parameter
     * @param result
     * @param methodMeta
     */
    public static void replayMode(Object parameter, Object result, MethodMeta methodMeta) {
        String querySql = "select * from qa_auto_assert where method_name=? and method_param=?";
        Object[] queryParam = {methodMeta.getMethodName(), JSON.toJSONString(parameter)};
        QaAutoAssert autoAssert = CIDbUtils.queryQaAutoAssetValue(querySql, queryParam);
        if (autoAssert == null) {
            Reporter.log("[AssertCheck#replayMode()]:{未录制有预期断言结果，请先执行断言录制！}");
            throw new AssertCheckException("未录制有预期断言结果，请先执行断言录制！");
        }
        JSONObject expectedData = JSON.parseObject(autoAssert.getMethod_return());
        JSONObject actualData = JSON.parseObject(JSON.toJSONString(result));
        Assert.assertEquals(expectedData, actualData);
    }

    /**
     * 执行录制断言与实际结果比较 - Http接口
     * @param result     Http接口请求结果
     * @param methodName Http接口测试类名
     * @param context    beforeTest传递参数
     * @param callback   回调函数，为testCase方法传入，入参和返回结果
     */
    public static void replayMode(String result, String methodName, final Map<String, Object> context, ITestResultCallback callback) throws Exception {
        try {
            JSONObject.parse(result);
        } catch (Exception e) {
            Reporter.log("[AssertCheck#handleException()]:{测试结果转换JSONObject异常！}");
            ResultAssert.assertResult(result, context, callback);
        }
        String querySql = "select * from qa_auto_assert where method_name=? and method_param=?";
        Object[] queryParam = {methodName, context.get(Constants.PARAMETER_NAME_).toString()};
        QaAutoAssert autoAssert = CIDbUtils.queryQaAutoAssetValue(querySql, queryParam);
        if (autoAssert == null) {
            Reporter.log("[AssertCheck#replayMode()]:{未录制有预期断言结果，请先执行断言录制！}");
            throw new AssertCheckException("未录制有预期断言结果，请先执行断言录制！");
        }
        Assert.assertTrue(autoAssert.getMethod_return().equals(result));
    }

    private static void insertAssertValue(String methodName, Object parameter, Object result) {
        String insertSql = "insert into qa_auto_assert (method_name,method_param,method_return,create_time) values(?,?,?,?)";
        Object[] insertParam = {methodName, JSON.toJSONString(parameter), JSON.toJSONString(result), new Date()};
        CIDbUtils.updateValue(insertSql, insertParam);
    }

    /**
     * 获取录制的预期返回值
     * @param methodName
     * @param param
     * @return
     */
    public static String getExpectedReturn(String methodName, String param) {
        String querySql = "select * from qa_auto_assert where method_name=? and method_param=?";
        Object[] queryParam = {methodName, param};
        QaAutoAssert autoAssert = CIDbUtils.queryQaAutoAssetValue(querySql, queryParam);
        if (autoAssert != null) {
            return autoAssert.getMethod_return();
        }
        return null;
    }
}
