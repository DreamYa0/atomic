package com.atomic.tools.assertcheck;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atomic.param.Constants;
import com.atomic.param.ITestResultCallback;
import com.atomic.param.ObjUtils;
import com.atomic.param.ParamUtils;
import com.atomic.tools.assertcheck.assertor.Assertor;
import com.atomic.tools.assertcheck.assertor.AssertorFactory;
import com.atomic.tools.assertcheck.assertor.RestfulAssertor;
import com.atomic.tools.assertcheck.assertor.UnitTestAssertor;
import com.atomic.util.ReflectionUtils;
import com.g7.framework.common.dto.BaseResult;
import com.g7.framework.common.dto.PagedResult;
import com.g7.framework.common.dto.Result;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.restassured.response.Response;
import org.apache.http.HttpEntity;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.reflect.FieldUtils.readField;

/**
 * ┌─┐       ┌─┐
 * ┌──┘ ┴───────┘ ┴──┐
 * │                 │
 * │       ───       │
 * │  ─┬┘       └┬─  │
 * │                 │
 * │       ─┴─       │
 * │                 │
 * └───┐         ┌───┘
 * │         │
 * │         │ 神兽保佑
 * │         │代码无BUG!
 * │         └──────────────┐
 * │                        │
 * │                        ├─┐
 * │                        ┌─┘
 * │                        │
 * └─┐  ┐  ┌───────┬──┐  ┌──┘
 * │ ─┤ ─┤       │ ─┤ ─┤
 * └──┴──┘       └──┴──┘
 * <p>
 * 通用结果断言类
 * @author dreamyao
 * @version v2.0.0
 * @date 2018/05/30 10:48
 */
public final class AssertResult {

    private AssertResult() {

    }

    /**
     * 测试结果断言
     * @param result     返回结果对象
     * @param context      入参excel集合
     * @param callback   回调函数
     * @param parameters 入参对象
     * @throws Exception 异常
     */
    @SuppressWarnings("all")
    public static void assertResult(Object result,
                                    ITestResult testResult,
                                    Object instance,
                                    Map<String, Object> context,
                                    ITestResultCallback callback,
                                    Object... parameters) throws Exception {

        // 把入参和执行结果写入param中
        callback.afterTestMethod(context, result, parameters);
        // 如果是Result型，检测执行结果，assertResult不填的就不管，比如自动化测试
        if (context.get(Constants.ASSERT_RESULT) != null && result instanceof PagedResult) {

            PagedResult pagedResult = (PagedResult) result;
            assertCheck(pagedResult, context);

        } else if (context.get(Constants.ASSERT_RESULT) != null && result instanceof Result) {

            Result resultNew = (Result) result;
            assertCheck(resultNew, context, testResult, instance);

        } else if (context.get(Constants.ASSERT_RESULT) != null && result instanceof BaseResult) {

            BaseResult baseResult = (BaseResult) result;
            assertCheck(baseResult, context);

        } else {

            Assertor assertor = AssertorFactory.getAssertor(UnitTestAssertor.class);
            // 执行 excel 中 exceptResult sheet 页中的断言
            assertor.assertResult(testResult, result, instance);
            /*Reporter.log("------------------ 返回类型未继承BaseResult,请自行执行断言！------------------", true);
            System.out.println();*/

        }
    }

    @SuppressWarnings("all")
    private static void assertCheck(Result result,
                                    Map<String, Object> context,
                                    ITestResult testResult,
                                    Object instance) {

        // 执行初步基本断言，断言内容包括Success、Code、Description
        if (ParamUtils.isExpectSuccess(context)) {
            Assert.assertTrue(result.isSuccess());
            //自动断言excel中expectedResult字段当值
            Object data = result.getData();
            autoAssertResult(data, context);

            Assertor assertor = AssertorFactory.getAssertor(UnitTestAssertor.class);
            // 执行 excel 中 exceptResult sheet 页中的断言
            assertor.assertResult(testResult, result, instance);

        } else if (isExpectFalse(context)) {
            assertCodeAndDec(result, context);
        }
    }

    private static void assertCheck(BaseResult result, Map<String, Object> context) {
        // 异常流程不进行expectedResult断言
        if (ParamUtils.isExpectSuccess(context)) {
            Assert.assertTrue(result.isSuccess());
        } else if (isExpectFalse(context)) {
            assertCodeAndDec(result, context);
        }
    }

    public static boolean isExpectFalse(Map<String, Object> context) {
        // 是否期望结果为N
        return Constants.EXCEL_NO.equals(context.get(Constants.ASSERT_RESULT));
    }

    @SuppressWarnings("all")
    private static void assertCheck(PagedResult result, Map<String, Object> context) {
        // 针对 PagedResult 返回值对象进行断言
        assertCheck((BaseResult) result, context);
        if (isExpectedResultNoNull(context)) {
            //自动断言excel中expectedResult字段当值
            List<Field> fields = ReflectionUtils.getAllFieldsList(result.getClass());
            List<Field> newField = fields.stream().filter(field -> "data".equals(field.getName())).collect(toList());
            Map<String, Object> map = Maps.newHashMap();
            map.put("data", context.get("expectedResult"));
            handleParameterTypeFields(newField, result, map);
        }
    }

    private static void assertCodeAndDec(BaseResult result, Map<String, Object> context) {
        // 异常流程断言Code和Description
        Assert.assertFalse(result.isSuccess());
        //增加期望为 N 时对Code和Description的断言
        if (isCodeNoNull(context)) {
            Assert.assertEquals(result.getCode(), context.get(Constants.ASSERT_CODE));
        }
        if (isDescriptionNoNull(context)) {
            Assert.assertEquals(result.getDescription(), context.get(Constants.ASSERT_MSG));
        }
    }

    private static boolean isDescriptionNoNull(Map<String, Object> context) {
        // 当期望Description不为空时返回true
        return context.get(Constants.ASSERT_MSG) != null && !"".equals(context.get(Constants.ASSERT_MSG));
    }

    private static boolean isCodeNoNull(Map<String, Object> context) {
        // 当期望Code不为空时返回true
        return context.get(Constants.ASSERT_CODE) != null && !"".equals(context.get(Constants.ASSERT_CODE));
    }

    private static boolean isExpectedResultNoNull(Map<String, Object> context) {
        // 当预期断言内容不为空时，进行自动断言
        return context.get(Constants.EXPECTED_RESULT) != null && !"".equals(context.get(Constants.EXPECTED_RESULT));
    }

    /**
     * 自动断言excel中expectedResult字段的值
     * @param actualData 实际返回值
     * @param param      入参
     */
    private static void autoAssertResult(Object actualData, Map<String, Object> param) {
        if (isExpectedResultNoNull(param)) {
            String expectedData = (String) param.get(Constants.EXPECTED_RESULT);
            // 判断 actualData 是否为基本类型
            if (ObjUtils.isBasicType(actualData.getClass())) {
                Assert.assertEquals(expectedData, actualData.toString());
                return;
            }
            Map<String, Object> expecDataMap = JSON.parseObject(expectedData,
                    new TypeReference<Map<String, Object>>() {
            });
            List<Field> fields = ReflectionUtils.getAllFieldsList(actualData.getClass());
            List<Field> basicFields = fields.stream().filter(
                    field -> ObjUtils.isBasicType(field.getType())).collect(toList());
            //基本类型属性断言
            basicFields.forEach(field -> {
                if (expecDataMap.containsKey(field.getName())) {
                    assertBasicTypeParam(field, actualData, expecDataMap);
                }
            });
            fields.removeAll(basicFields);
            // List集合断言
            List<Field> parameterTypeFields = fields.stream()
                    .filter(f -> f.getGenericType() instanceof ParameterizedType)
                    .collect(toList());
            handleParameterTypeFields(parameterTypeFields, actualData, expecDataMap);
            fields.removeAll(parameterTypeFields);
            // 数组类型断言
            List<Field> arrayTypeFields = fields.stream()
                    .filter(f -> f.getGenericType() instanceof GenericArrayType)
                    .collect(toList());
            arrayTypeFields.forEach(field -> {
                if (expecDataMap.containsKey(field.getName())) {
                    assertArrayTypeParam(field, actualData, expecDataMap);
                }
            });
            fields.removeAll(arrayTypeFields);
            // 自定义对象断言
            for (Field field : fields) {
                try {
                    if (expecDataMap.containsKey(field.getName())) {
                        assertClassTypeParam(field, actualData, expecDataMap);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 断言数组类型属性的值
     * @param field        字段对应的Field
     * @param data         Result<T> T 或 T 中所嵌套的基本类型或包装器
     * @param expecDataMap 预期结果的 map 集合
     */
    private static void assertArrayTypeParam(Field field, Object data, Map<String, Object> expecDataMap) {
        try {
            // 实际值
            Object[] actArraysData = (Object[]) readField(field, data, true);
            Object expData = expecDataMap.get(field.getName());
            if (expData != null) {
                Object[] expArrayData = JSON.parseObject(expData.toString(), new TypeReference<Object[]>() {
                });
                Assert.assertTrue(expArrayData.length <= actArraysData.length);
                if (ObjUtils.isBasicType(actArraysData[0].getClass())) {
                    List<Object> actList = Lists.newArrayList(Arrays.asList(actArraysData));
                    List<Object> expList = Lists.newArrayList(Arrays.asList(expArrayData));
                    expList.forEach(o -> Assert.assertTrue(actList.contains(o)));
                }
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 断言基本类型属性的值
     * @param field        字段对应的Field
     * @param actualData   Result<T> T 或 T 中所嵌套的基本类型或包装器
     * @param expecDataMap 预期结果的 map 集合
     */
    private static void assertBasicTypeParam(Field field, Object actualData, Map<String, Object> expecDataMap) {
        try {
            // 实际值
            Object actData = readField(field, actualData, true);
            // 预期值
            Object expData = expecDataMap.get(field.getName());
            if (expData != null) {
                boolean result = Objects.deepEquals(actData.toString(), expData.toString());
                Assert.assertTrue(result);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 断言自定义对象
     * @param field        字段对应的Field
     * @param actualData   Result<T> T 或 T 中所嵌套的自定义对象
     * @param expecDataMap 预期结果的 map 集合
     * @throws IllegalAccessException 非法访问异常
     */
    private static void assertClassTypeParam(Field field,
                                             Object actualData,
                                             Map<String, Object> expecDataMap) throws IllegalAccessException {
        // 对象类型属性的实际值
        Object result = readField(field, actualData, true);
        Map<String, Object> mapData = JSON.parseObject(expecDataMap.get(field.getName()).toString(),
                new TypeReference<Map<String, Object>>() {
        });
        if (mapData == null) {
            return;
        }
        List<Field> fields = ReflectionUtils.getAllFieldsList(result.getClass());
        // 基本类型属性断言
        List<Field> basicFields = fields.stream().filter(f -> ObjUtils.isBasicType(f.getType())).collect(toList());
        basicFields.forEach(f -> {
            if (mapData.containsKey(f.getName())) {
                assertBasicTypeParam(f, result, mapData);
            }
        });
        fields.removeAll(basicFields);
        // List集合断言
        List<Field> paramterTypeFields = fields.stream()
                .filter(f -> f.getGenericType() instanceof ParameterizedType)
                .collect(toList());
        handleParameterTypeFields(paramterTypeFields, result, mapData);
        // 自定义对象断言
        fields.removeAll(paramterTypeFields);
        // 数组类型断言
        List<Field> arrayTypeFields = fields.stream()
                .filter(f -> f.getGenericType() instanceof GenericArrayType)
                .collect(toList());
        arrayTypeFields.forEach(f -> {
            if (mapData.containsKey(f.getName())) {
                assertArrayTypeParam(f, result, mapData);
            }
        });
        fields.removeAll(arrayTypeFields);
        fields.forEach(classField -> {
            try {
                if (mapData.containsKey(classField.getName())) {
                    assertClassTypeParam(classField, result, mapData);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 断言List集合
     * @param parameterTypeFields 属性为List的字段的Field集合
     * @param o                   Result<T> T 或 T 中嵌套的自定义对象
     * @param map                 预期结果的 map 集合
     */
    private static void handleParameterTypeFields(List<Field> parameterTypeFields,
                                                  Object o,
                                                  Map<String, Object> map) {

        parameterTypeFields.forEach(f -> {
            try {
                if (map.containsKey(f.getName())) {
                    assertListTypeParam(f, o, map);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 断言list集合
     * @param field        字段
     * @param actualData   Result<T> 中的 T
     * @param expecDataMap 预期结果的 map 集合
     */
    @SuppressWarnings("unchecked")
    private static void assertListTypeParam(Field field,
                                            Object actualData,
                                            Map<String, Object> expecDataMap) throws IllegalAccessException {

        List<Object> actualResult = (List<Object>) readField(field, actualData, true);
        List<Object> expecResult = JSON.parseObject(expecDataMap.get(field.getName()).toString(),
                new TypeReference<List<Object>>() {
        });
        if (expecResult == null) {
            return;
        }
        Assert.assertTrue(expecResult.size() <= actualResult.size(),
                "某个字段实际返回的List集合的长度小于预期结果中的长度");
        // 判断List中是否存放的是基本类型，是则执行基本类型数据断言
        for (int i = 0; i < expecResult.size(); i++) {
            Object o = expecResult.get(i);
            Object actObject = actualResult.get(i);
            if (ObjUtils.isBasicType(actObject.getClass())) {
                if (actualResult.size() == expecResult.size()) {
                    Assert.assertTrue(Objects.deepEquals(actualResult.toString(), expecResult.toString()));
                } else {
                    boolean result = actualResult.toString().contains(o.toString());
                    Assert.assertTrue(result, "某个字段实际返回的List集合中不包含预期结果值");
                }
            } else {
                // 实现List中存放自定义对象的断言
                Map<String, Object> maps = JSON.parseObject(o.toString(), new TypeReference<Map<String, Object>>() {
                });
                List<Field> fields = ReflectionUtils.getAllFieldsList(actObject.getClass());
                List<Field> basicFields = fields.stream()
                        .filter(f -> ObjUtils.isBasicType(f.getType()))
                        .collect(toList());
                basicFields.forEach(f -> {
                    if (maps.containsKey(f.getName())) {
                        assertBasicTypeParam(f, actObject, maps);
                    }
                });
                fields.removeAll(basicFields);
                // List集合断言
                List<Field> parameterTypeFields = fields.stream()
                        .filter(f -> f.getGenericType() instanceof ParameterizedType)
                        .collect(toList());
                handleParameterTypeFields(parameterTypeFields, actObject, maps);
                // 自定义对象断言
                fields.removeAll(parameterTypeFields);
                // 数组类型断言
                List<Field> arrayTypeFields = fields.stream()
                        .filter(f -> f.getGenericType() instanceof GenericArrayType)
                        .collect(toList());
                arrayTypeFields.forEach(f -> {
                    if (maps.containsKey(f.getName())) {
                        assertArrayTypeParam(f, o, maps);
                    }
                });
                fields.removeAll(arrayTypeFields);
                fields.forEach(f -> {
                    try {
                        assertClassTypeParam(f, o, maps);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    /**
     * HttP接口测试结果断言
     * @param result   Http接口返回值
     * @param context  Excel测试用例字段
     * @param callback 回调函数
     * @throws Exception 异常
     */
    public static void assertResult(final Object result,
                                    final Map<String, Object> context,
                                    ITestResultCallback callback) throws Exception {

        // 把入参和执行结果写入param中
        callback.afterTestMethod(context, result, context.get(Constants.PARAMETER_NAME_));
        // result为HttpEntity实现类，则跳过自动断言
        if (result instanceof HttpEntity) {
            Reporter.log("测试结果需手动断言！", true);
        } else {
            // expectedResult不填的就不管
            if (isExpectedResultNoNull(context)) {
                String expectJsonResult = context.get(Constants.EXPECTED_RESULT).toString();
                String actualResult = result.toString();

                Map<String, Object> expectMap = JSON.parseObject(expectJsonResult,
                        new TypeReference<Map<String, Object>>() {
                });
                Map<String, Object> actualMap = JSON.parseObject(actualResult,
                        new TypeReference<Map<String, Object>>() {
                });

                Assert.assertTrue(expectMap.size() <= actualMap.size());
                List<String> expectKeys = Lists.newArrayList(expectMap.keySet());
                List<String> actualKeys = Lists.newArrayList(actualMap.keySet());
                actualKeys.removeAll(expectKeys);
                actualKeys.forEach(actualMap::remove);

                handleMap(expectMap, actualMap);

                expectMap.forEach((key, value) -> Assert.assertEquals(actualMap.get(key).toString(), value.toString()));
            } else {
                Reporter.log("测试结果需手动断言！", true);
            }
        }
    }

    /**
     * REST 风格接口自动断言
     * @param response   rest请求响应结果
     * @param context    入参上下文
     * @param callback   回调
     * @throws Exception 异常
     */
    public static void assertResultForRest(Response response,
                                           ITestResult testResult,
                                           Object instance,
                                           Map<String, Object> context,
                                           ITestResultCallback callback) throws Exception {

        // 把入参和执行结果写入param中
        callback.afterTestMethod(context, response, context.get(Constants.PARAMETER_NAME_));
        // Assert.assertEquals(response.getStatusCode(), 200);
        if (isExpectedResultNoNull(context)) {
            String expectJsonResult = context.get(Constants.EXPECTED_RESULT).toString();
            String actualResult = response.asString();
            Map<String, Object> expectMap = JSON.parseObject(expectJsonResult,
                    new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> actualMap = JSON.parseObject(actualResult, new TypeReference<Map<String, Object>>() {
            });
            Assert.assertTrue(expectMap.size() <= actualMap.size());
            List<String> expectKeys = Lists.newArrayList(expectMap.keySet());
            List<String> actualKeys = Lists.newArrayList(actualMap.keySet());
            actualKeys.removeAll(expectKeys);
            actualKeys.forEach(actualMap::remove);
            handleMap(expectMap, actualMap);
            expectMap.forEach((key, value) -> Assert.assertEquals(actualMap.get(key).toString(), value.toString()));
        }

        // 执行 excel 中 exceptResult sheet 页中的断言
        Assertor assertor = AssertorFactory.getAssertor(RestfulAssertor.class);
        assertor.assertResult(testResult, response, instance);
    }

    private static void handleMap(Map<String, Object> expectMap, Map<String, Object> actualMap) {
        if (expectMap.get("data") != null || actualMap.get("data") != null) {
            Map<String, Object> expectDataMap = JSON.parseObject(expectMap.get("data").toString(),
                    new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> actualDataMap = JSON.parseObject(actualMap.get("data").toString(),
                    new TypeReference<Map<String, Object>>() {
            });
            assertMapForRest(expectDataMap, actualDataMap);
            actualMap.remove("data");
            expectMap.remove("data");
        }
    }

    private static void assertMapForRest(Map<String, Object> newExpectMap, Map<String, Object> newActualMap) {
        newExpectMap.forEach((key, value) -> {
            try {
                if (value == null) {
                    return;
                }
                // 转换为Map的方式处理
                Map<String, Object> expectMap = JSON.parseObject(value.toString(),
                        new TypeReference<Map<String, Object>>() {
                });
                Map<String, Object> actualMap = JSON.parseObject(newActualMap.get(key).toString(),
                        new TypeReference<Map<String, Object>>() {
                });
                List<String> expectKeys = Lists.newArrayList(expectMap.keySet());
                List<String> actualKeys = Lists.newArrayList(actualMap.keySet());
                Assert.assertTrue(expectKeys.size() <= actualKeys.size());
                Map<String, Object> expectDataMap = Maps.newHashMap();
                Map<String, Object> actualDataMap = Maps.newHashMap();
                for (int i = 0; i < expectKeys.size(); i++) {
                    expectDataMap.put(expectKeys.get(i), expectMap.get(expectKeys.get(i)));
                    if (actualKeys.contains(expectKeys.get(i))) {
                        actualDataMap.put(expectKeys.get(i), actualMap.get(expectKeys.get(i)));
                    }
                }
                Assert.assertEquals(actualDataMap.size(), expectDataMap.size());
                assertMapForRest(expectDataMap, actualDataMap);
            } catch (Exception e) {
                try {
                    // 转换为List的方式处理
                    List<Object> expectList = JSON.parseObject(value.toString(), new TypeReference<List<Object>>() {
                    });
                    List<Object> actualList = JSON.parseObject(newActualMap.get(key).toString(),
                            new TypeReference<List<Object>>() {
                    });
                    Assert.assertTrue(expectList.size() <= actualList.size());
                    for (int i = 0; i < expectList.size(); i++) {
                        Object o = expectList.get(i);
                        Object actObject = actualList.get(i);
                        JSONObject expectJsonObject = (JSONObject) o;
                        JSONObject actualJsonObject = (JSONObject) actObject;
                        List<String> expectKeys = Lists.newArrayList(expectJsonObject.keySet());
                        List<String> actualKeys = Lists.newArrayList(actualJsonObject.keySet());
                        Assert.assertTrue(expectKeys.size() <= actualKeys.size());
                        Map<String, Object> expectMap = Maps.newHashMap();
                        Map<String, Object> actualMap = Maps.newHashMap();
                        for (int j = 0; j < expectKeys.size(); j++) {
                            expectMap.put(expectKeys.get(j), expectJsonObject.get(expectKeys.get(j)));
                            if (actualKeys.contains(expectKeys.get(j))) {
                                actualMap.put(expectKeys.get(j), actualJsonObject.get(actualKeys.get(j)));
                            }
                        }
                        Assert.assertEquals(actualMap.size(), expectMap.size());
                        assertMapForRest(expectMap, actualMap);
                        /*Assert.assertTrue(jsonEquals(o.toString(), actObject.toString()));*/
                    }
                } catch (Exception e1) {
                    try {
                        // 转换为数组的方式处理
                        Object[] expectArray = JSON.parseObject(value.toString(), new TypeReference<Object[]>() {
                        });
                        Object[] actualArray = JSON.parseObject(newActualMap.get(key).toString(),
                                new TypeReference<Object[]>() {
                        });
                        List<Object> expList = Lists.newArrayList(Arrays.asList(expectArray));
                        List<Object> actList = Lists.newArrayList(Arrays.asList(actualArray));
                        Assert.assertTrue(expList.size() <= actList.size());
                        expList.forEach(o -> Assert.assertTrue(actList.contains(o)));
                    } catch (Exception e2) {
                        // 以基本类型的方式处理
                        Assert.assertEquals(newActualMap.get(key).toString(), value.toString());
                    }
                }
            }
        });
    }
}
