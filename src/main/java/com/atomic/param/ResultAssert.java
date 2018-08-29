package com.atomic.param;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atomic.assertor.Assertor;
import com.atomic.assertor.AssertorFactory;
import com.atomic.assertor.RestfulAssertor;
import com.atomic.assertor.UnitTestAssertor;
import com.atomic.exception.ExceptionUtils;
import com.atomic.util.ReflectionUtils;import com.exc.common.dto.BaseResult;
import com.exc.common.dto.PagedResult;
import com.exc.common.dto.Result;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.restassured.response.Response;
import org.apache.http.HttpEntity;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * @Data 2018/05/30 10:48
 */
public final class ResultAssert {

    private ResultAssert() {
    }

    /**
     * 测试结果断言
     * @param result     返回结果对象
     * @param context      入参excel集合
     * @param callback   回调函数
     * @param parameters 入参对象
     * @throws Exception 异常
     */
    public static void assertResult(Object result, ITestResult testResult, Object instance, Map<String, Object> context, ITestResultCallback callback, Object... parameters) throws Exception {
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

    /**
     * 执行初步基本断言，断言内容包括Success、Code、Description
     * @param result 返回结果对象
     * @param context  入参excel集合
     */
    private static void assertCheck(Result result, Map<String, Object> context, ITestResult testResult, Object instance) {
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

    /**
     * 异常流程不进行expectedResult断言
     * @param result 返回值对象
     * @param context  入参
     */
    private static void assertCheck(BaseResult result, Map<String, Object> context) {
        if (ParamUtils.isExpectSuccess(context)) {
            Assert.assertTrue(result.isSuccess());
        } else if (isExpectFalse(context)) {
            assertCodeAndDec(result, context);
        }
    }

    /**
     * 是否期望结果为N
     * @param context 入参
     * @return
     */
    public static boolean isExpectFalse(Map<String, Object> context) {
        return Constants.EXCEL_NO.equals(context.get(Constants.ASSERT_RESULT));
    }

    /**
     * 针对 PagedResult 返回值对象进行断言
     * @param result 返回值对象
     * @param context  入参
     */
    @SuppressWarnings("unchecked")
    private static void assertCheck(PagedResult result, Map<String, Object> context) {
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

    /**
     * 异常流程断言Code和Description
     * @param result 返回结果对象
     * @param context  入参excel集合
     */
    private static void assertCodeAndDec(BaseResult result, Map<String, Object> context) {
        Assert.assertFalse(result.isSuccess());
        //增加期望为 N 时对Code和Description的断言
        if (isCodeNoNull(context)) {
            Assert.assertEquals(result.getCode(), context.get(Constants.ASSERT_CODE));
        }
        if (isDescriptionNoNull(context)) {
            Assert.assertEquals(result.getDescription(), context.get(Constants.ASSERT_MSG));
        }
    }

    /**
     * 当期望Description不为空时返回true
     * @param context 入参
     * @return
     */
    private static boolean isDescriptionNoNull(Map<String, Object> context) {
        if (context.get(Constants.ASSERT_MSG) == null || "".equals(context.get(Constants.ASSERT_MSG))) {
            return false;
        }
        return true;
    }

    /**
     * 当期望Code不为空时返回true
     * @param context 入参
     * @return
     */
    private static boolean isCodeNoNull(Map<String, Object> context) {
        if (context.get(Constants.ASSERT_CODE) == null || "".equals(context.get(Constants.ASSERT_CODE))) {
            return false;
        }
        return true;
    }

    /**
     * 当预期断言内容不为空时，进行自动断言
     * @param context 入参
     * @return
     */
    private static boolean isExpectedResultNoNull(Map<String, Object> context) {
        if (context.get(Constants.EXPECTED_RESULT) == null || "".equals(context.get(Constants.EXPECTED_RESULT))) {
            return false;
        }
        return true;
    }

    /**
     * 执行预期expectedResult内容断言，完成智能化断言
     * @param result 返回结果对象
     * @param param  入参excel集合
     * @return True or False
     */
    @Deprecated
    private static boolean assertExpectedResult(Result result, Map<String, Object> param, Type returnType) {
        String expectedData = (String) param.get(Constants.EXPECTED_RESULT);
        if (returnType instanceof ParameterizedType) {
            Type returnDataType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
            if (returnDataType instanceof ParameterizedType) {
                //当data为List时 具体功能待实现
                String jsonActualData = JSON.toJSONString(result.getData());
                String jsonExpecData = JSON.toJSONString(expectedData);
                if (jsonActualData.contains(jsonExpecData)) {
                    return true;
                }
                System.out.println("------------------------返回值Result中的data值为List数据，请自行实现手动断言！----------------------");
                return true;
            } else if (returnDataType instanceof Class) {
                if (StringUtils.isBasicType((Class) returnDataType)) {
                    //当data为基本类型时
                    if (!expectedData.equals(result.getData().toString())) {
                        return false;
                    }
                } else {
                    //当data为对象时
                    Map<String, String> expecDataMap = JSON.parseObject(expectedData, new TypeReference<Map<String, String>>() {
                    });
                    Field[] fields = ((Class) returnDataType).getDeclaredFields();
                    for (Field field : fields) {
                        try {
                            Type type = field.getGenericType();
                            String fieldGetName = parGetName(field.getName(), field.getType().getSimpleName());
                            String fieldIsGetName = parIsGetName(field.getName());
                            Method fieldGetMethod;
                            if (ReflectionUtils.getMethod(result.getData().getClass(), fieldGetName) != null) {
                                fieldGetMethod = ReflectionUtils.getMethod(result.getData().getClass(), fieldGetName);
                            } else if (ReflectionUtils.getMethod(result.getData().getClass(), fieldIsGetName) != null) {
                                fieldGetMethod = ReflectionUtils.getMethod(result.getData().getClass(), fieldIsGetName);
                            } else {
                                continue;
                            }
                            Object actualValue = fieldGetMethod.invoke(result.getData());
                            if (StringUtils.isBasicType(field.getType())) {
                                String expecValue = expecDataMap.get(field.getName());
                                if (expecValue == null || "".equals(expecValue)) {
                                    continue;
                                }
                                if (!expecValue.equals(actualValue.toString())) {
                                    return false;
                                }
                            } else if (type instanceof Class) {
                                String fieldName = field.getName();
                                String value = expecDataMap.get(fieldName);
                                Map<String, String> expecMap = JSON.parseObject(value, new TypeReference<Map<String, String>>() {
                                });
                                Field[] fields1 = ((Class) type).getDeclaredFields();
                                for (Field field1 : fields1) {
                                    Type type1 = field1.getGenericType();
                                    String fieldGetName1 = parGetName(field1.getName(), field.getType().getSimpleName());
                                    String fieldIsGetName1 = parIsGetName(field1.getName());
                                    Method fieldGetMethod1;
                                    if (ReflectionUtils.getMethod(actualValue.getClass(), fieldGetName1) != null) {
                                        fieldGetMethod1 = ReflectionUtils.getMethod(actualValue.getClass(), fieldGetName1);
                                    } else if (ReflectionUtils.getMethod(actualValue.getClass(), fieldIsGetName1) != null) {
                                        fieldGetMethod1 = ReflectionUtils.getMethod(actualValue.getClass(), fieldIsGetName1);
                                    } else {
                                        continue;
                                    }
                                    Object actualValue1 = fieldGetMethod1.invoke(actualValue);
                                    if (StringUtils.isBasicType(field1.getType())) {
                                        String expecValue1 = expecMap.get(field1.getName());
                                        if (expecValue1 == null || "".equals(expecValue1)) {
                                            continue;
                                        }
                                        if (!expecValue1.equals(actualValue1.toString())) {
                                            return false;
                                        }
                                    } else if (type1 instanceof Class) {
                                        String fieldName1 = field1.getName();
                                        String value1 = expecMap.get(fieldName1);
                                        Map<String, String> expecMap1 = JSON.parseObject(value1, new TypeReference<Map<String, String>>() {
                                        });
                                        Field[] fields2 = ((Class) type1).getDeclaredFields();
                                        for (Field field2 : fields2) {
                                            String fieldGetName2 = parGetName(field2.getName(), field.getType().getSimpleName());
                                            String fieldIsGetName2 = parIsGetName(field2.getName());
                                            Method fieldGetMethod2;
                                            if (ReflectionUtils.getMethod(actualValue1.getClass(), fieldGetName2) != null) {
                                                fieldGetMethod2 = ReflectionUtils.getMethod(actualValue1.getClass(), fieldGetName2);
                                            } else if (ReflectionUtils.getMethod(actualValue1.getClass(), fieldIsGetName2) != null) {
                                                fieldGetMethod2 = ReflectionUtils.getMethod(actualValue1.getClass(), fieldIsGetName2);
                                            } else {
                                                continue;
                                            }
                                            Object actualValue2 = fieldGetMethod2.invoke(actualValue1);
                                            if (StringUtils.isBasicType(field2.getType())) {
                                                String expecValue2 = expecMap1.get(field2.getName());
                                                if (expecValue2 == null || "".equals(expecValue2)) {
                                                    continue;
                                                }
                                                if (!expecValue2.equals(actualValue2.toString())) {
                                                    return false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Reporter.log("[ResultAssert#assertExpectedResult()]:{获取返回实际值异常！！}", true);
                        }
                    }
                }
                return true;
            }
        } else {
            Reporter.log("[ResultAssert#assertExpectedResult()]:{方法返回值不是Result<T>类型！}");
            throw new RuntimeException("方法返回值不是Result<T>类型");
        }
        return false;
    }

    /**
     * 拼接boolean字段方法
     * @param fieldName
     * @return
     */
    private static String parIsGetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 拼接在某属性的 get方法
     * @param fieldName
     * @return
     */
    private static String parGetName(String fieldName, String fieldType) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        if (("Boolean".equals(fieldType) || "boolean".equals(fieldType)) && fieldName.startsWith("is")) {
            fieldName = fieldName.substring(2);
        }
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 自动断言excel中expectedResult字段当值
     * @param result     实际返回值
     * @param param      入参
     * @param returnType 返回值对象的Type属性
     */
    private static void autoAssertResult(Result result, Map<String, Object> param, Type returnType) {
        if (isExpectedResultNoNull(param)) {
            String expectedData = (String) param.get(Constants.EXPECTED_RESULT);
            Object actualData = result.getData();
            Type returnDataType = ((ParameterizedType) returnType).getActualTypeArguments()[0];//获取Result<T> 中的类型 T的Type类型
            if (StringUtils.isBasicType(actualData.getClass())) {
                Assert.assertTrue(Objects.equals(actualData.toString(), expectedData));
            } else if (returnDataType instanceof Class) {
                Object expecData = JSON.parseObject(expectedData, returnDataType);
                List<Field> expecFields = ReflectionUtils.getAllFieldsList(expecData.getClass());
                List<Field> basicExpecFields = expecFields.stream().filter(field -> StringUtils.isBasicType(field.getType())).collect(toList());
                basicExpecFields.forEach(field -> assertBasicTypeParam(field, actualData, expecData));
            }
        }
    }

    private static void assertBasicTypeParam(Field field, Object actualData, Object expecData) {
        try {
            boolean result = Objects.deepEquals(readField(field, actualData, true), readField(field, expecData, true));
            Assert.assertTrue(result);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
            if (StringUtils.isBasicType(actualData.getClass())) {
                Assert.assertTrue(Objects.equals(actualData.toString(), expectedData));
                return;
            }
            Map<String, Object> expecDataMap = JSON.parseObject(expectedData, new TypeReference<Map<String, Object>>() {
            });
            List<Field> fields = ReflectionUtils.getAllFieldsList(actualData.getClass());
            List<Field> basicFields = fields.stream().filter(field -> StringUtils.isBasicType(field.getType())).collect(toList());
            //基本类型属性断言
            basicFields.forEach(field -> {
                if (expecDataMap.keySet().contains(field.getName())) {
                    assertBasicTypeParam(field, actualData, expecDataMap);
                }
            });
            fields.removeAll(basicFields);
            // List集合断言
            List<Field> parameterTypeFields = fields.stream().filter(f -> f.getGenericType() instanceof ParameterizedType).collect(toList());
            handleParameterTypeFields(parameterTypeFields, actualData, expecDataMap);
            fields.removeAll(parameterTypeFields);
            // 数组类型断言
            List<Field> arrayTypeFields = fields.stream().filter(f -> f.getGenericType() instanceof GenericArrayType).collect(toList());
            arrayTypeFields.forEach(field -> {
                if (expecDataMap.keySet().contains(field.getName())) {
                    assertArrayTypeParam(field, actualData, expecDataMap);
                }
            });
            fields.removeAll(arrayTypeFields);
            // 自定义对象断言
            for (Field field : fields) {
                try {
                    if (expecDataMap.keySet().contains(field.getName())) {
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
                if (StringUtils.isBasicType(actArraysData[0].getClass())) {
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
    private static void assertClassTypeParam(Field field, Object actualData, Map<String, Object> expecDataMap) throws IllegalAccessException {
        // 对象类型属性的实际值
        Object result = readField(field, actualData, true);
        Map<String, Object> mapData = JSON.parseObject(expecDataMap.get(field.getName()).toString(), new TypeReference<Map<String, Object>>() {
        });
        if (mapData == null) {
            return;
        }
        List<Field> fields = ReflectionUtils.getAllFieldsList(result.getClass());
        // 基本类型属性断言
        List<Field> basicFields = fields.stream().filter(f -> StringUtils.isBasicType(f.getType())).collect(toList());
        basicFields.forEach(f -> {
            if (mapData.keySet().contains(f.getName())) {
                assertBasicTypeParam(f, result, mapData);
            }
        });
        fields.removeAll(basicFields);
        // List集合断言
        List<Field> paramterTypeFields = fields.stream().filter(f -> f.getGenericType() instanceof ParameterizedType).collect(toList());
        handleParameterTypeFields(paramterTypeFields, result, mapData);
        // 自定义对象断言
        fields.removeAll(paramterTypeFields);
        // 数组类型断言
        List<Field> arrayTypeFields = fields.stream().filter(f -> f.getGenericType() instanceof GenericArrayType).collect(toList());
        arrayTypeFields.forEach(f -> {
            if (mapData.keySet().contains(f.getName())) {
                assertArrayTypeParam(f, result, mapData);
            }
        });
        fields.removeAll(arrayTypeFields);
        fields.forEach(classField -> {
            try {
                if (mapData.keySet().contains(classField.getName())) {
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
    private static void handleParameterTypeFields(List<Field> parameterTypeFields, Object o, Map<String, Object> map) {
        parameterTypeFields.forEach(f -> {
            try {
                if (map.keySet().contains(f.getName())) {
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
    private static void assertListTypeParam(Field field, Object actualData, Map<String, Object> expecDataMap) throws IllegalAccessException {
        List<Object> actualResult = (List<Object>) readField(field, actualData, true);
        List<Object> expecResult = JSON.parseObject(expecDataMap.get(field.getName()).toString(), new TypeReference<List<Object>>() {
        });
        if (expecResult == null) {
            return;
        }
        Assert.assertTrue(expecResult.size() <= actualResult.size(), "某个字段实际返回的List集合的长度小于预期结果中的长度");
        // 判断List中是否存放的是基本类型，是则执行基本类型数据断言
        for (int i = 0; i < expecResult.size(); i++) {
            Object o = expecResult.get(i);
            Object actObject = actualResult.get(i);
            if (StringUtils.isBasicType(actObject.getClass())) {
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
                List<Field> basicFields = fields.stream().filter(f -> StringUtils.isBasicType(f.getType())).collect(toList());
                basicFields.forEach(f -> {
                    if (maps.keySet().contains(f.getName())) {
                        assertBasicTypeParam(f, actObject, maps);
                    }
                });
                fields.removeAll(basicFields);
                // List集合断言
                List<Field> parameterTypeFields = fields.stream().filter(f -> f.getGenericType() instanceof ParameterizedType).collect(toList());
                handleParameterTypeFields(parameterTypeFields, actObject, maps);
                // 自定义对象断言
                fields.removeAll(parameterTypeFields);
                // 数组类型断言
                List<Field> arrayTypeFields = fields.stream().filter(f -> f.getGenericType() instanceof GenericArrayType).collect(toList());
                arrayTypeFields.forEach(f -> {
                    if (maps.keySet().contains(f.getName())) {
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
    public static void assertResult(final Object result, final Map<String, Object> context, ITestResultCallback callback) throws Exception {
        // 把入参和执行结果写入param中
        callback.afterTestMethod(context, result, context.get(Constants.PARAMETER_NAME_));
        // result为HttpEntity实现类，则跳过自动断言
        if (result instanceof HttpEntity) {
            Reporter.log("[ResultAssert#assertResult() ]:{测试结果需手动断言！}", true);
        } else {
            // expectedResult不填的就不管
            if (isExpectedResultNoNull(context)) {
                String expectJsonResult = context.get(Constants.EXPECTED_RESULT).toString();
                String actualResult = result.toString();

                Map<String, Object> expectMap = JSON.parseObject(expectJsonResult, new TypeReference<Map<String, Object>>() {
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
            } else {
                Reporter.log("[ResultAssert#assertResult() ]:{测试结果需手动断言！}", true);
            }
        }
    }

    /**
     * JSONObject对象断言目前不支持嵌套JSONObject,JSONArray的场景
     * @param expResult 预期结果
     * @param actResult 实际结果
     */
    private static void assertJSON(JSONObject expResult, JSONObject actResult) {
        boolean autoAssertStatus = true;
        Set<Map.Entry<String, Object>> jsonSet = actResult.entrySet();
        Iterator<Map.Entry<String, Object>> iterator = jsonSet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> jsonElements = iterator.next();
            jsonElements.getKey();
            if (jsonElements.getValue() instanceof JSONObject) {
                autoAssertStatus = false;
            }
            if (jsonElements.getValue() instanceof JSONArray) {
                autoAssertStatus = false;
            }
        }
        // 非嵌套JSONObject断言
        if (autoAssertStatus) {
            Assert.assertEquals(expResult, actResult);
        }
    }

    /**
     * REST 风格接口自动断言
     * @param response   rest请求响应结果
     * @param context    入参上下文
     * @param callback   回调
     * @throws Exception 异常
     */
    public static void assertResultForRest(Response response, ITestResult testResult, Object instance, Map<String, Object> context, ITestResultCallback callback) throws Exception {
        // 把入参和执行结果写入param中
        callback.afterTestMethod(context, response, context.get(Constants.PARAMETER_NAME_));
        // Assert.assertEquals(response.getStatusCode(), 200);
        if (isExpectedResultNoNull(context)) {
            String expectJsonResult = context.get(Constants.EXPECTED_RESULT).toString();
            String actualResult = response.asString();
            Map<String, Object> expectMap = JSON.parseObject(expectJsonResult, new TypeReference<Map<String, Object>>() {
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
            Map<String, Object> expectDataMap = JSON.parseObject(expectMap.get("data").toString(), new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> actualDataMap = JSON.parseObject(actualMap.get("data").toString(), new TypeReference<Map<String, Object>>() {
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
                Map<String, Object> expectMap = JSON.parseObject(value.toString(), new TypeReference<Map<String, Object>>() {
                });
                Map<String, Object> actualMap = JSON.parseObject(newActualMap.get(key).toString(), new TypeReference<Map<String, Object>>() {
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
                Assert.assertTrue(expectDataMap.size() == actualDataMap.size());
                assertMapForRest(expectDataMap, actualDataMap);
            } catch (Exception e) {
                try {
                    // 转换为List的方式处理
                    List<Object> expectList = JSON.parseObject(value.toString(), new TypeReference<List<Object>>() {
                    });
                    List<Object> actualList = JSON.parseObject(newActualMap.get(key).toString(), new TypeReference<List<Object>>() {
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
                        Assert.assertTrue(expectMap.size() == actualMap.size());
                        assertMapForRest(expectMap, actualMap);
                        /*Assert.assertTrue(jsonEquals(o.toString(), actObject.toString()));*/
                    }
                } catch (Exception e1) {
                    try {
                        // 转换为数组的方式处理
                        Object[] expectArray = JSON.parseObject(value.toString(), new TypeReference<Object[]>() {
                        });
                        Object[] actualArray = JSON.parseObject(newActualMap.get(key).toString(), new TypeReference<Object[]>() {
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

    /**
     * 比较两个json串是否相同
     * @param j1 第一个json串(json串中不能有换行)
     * @param j2 第二个json串(json串中不能有换行)
     * @return 布尔型比较结果
     */
    public static boolean jsonEquals(String j1, String j2) {
        //将json中表示list的[]替换成{}。思想：只保留层次结构，不区分类型
        //这样直接替换，可能会导致某些value中的符号也被替换，但是不影响结果，因为j1、j2的变化是相对的
        j1 = j1.replaceAll("\\[", "{");
        j1 = j1.replaceAll("]", "}");
        j2 = j2.replaceAll("\\[", "{");
        j2 = j2.replaceAll("]", "}");
        //将json中，字符串型的value中的{},字符替换掉，防止干扰(并没有去除key中的，因为不可能存在那样的变量名)
        //未转义regex：(?<=:")(([^"]*\{[^"]*)|([^"]*\}[^"]*)|([^"]*,[^"]*))(?=")
        Pattern pattern = Pattern.compile("(?<=:\")(([^\"]*\\{[^\"]*)|([^\"]*\\}[^\"]*)|([^\"]*,[^\"]*))(?=\")");
        j1 = cleanStr4Special(j1, pattern.matcher(j1));
        j2 = cleanStr4Special(j2, pattern.matcher(j2));
        //转义字符串value中的空格
        //未转义regex:"[^",]*?\s+?[^",]*?"
        pattern = Pattern.compile("\"[^\",]*?\\s+?[^\",]*?\"");
        j1 = cleanStr4Space(j1, pattern.matcher(j1));
        j2 = cleanStr4Space(j2, pattern.matcher(j2));
        //现在可以安全的全局性去掉空格
        j1 = j1.replaceAll(" ", "");
        j2 = j2.replaceAll(" ", "");
        //调用递归方法
        return compareAtom(j1, j2);
    }

    /**
     * 比较字符串核心递归方法
     * @param j1 Json字符串
     * @param j2 Json字符串
     * @return True or False
     */
    private static boolean compareAtom(String j1, String j2) {
        if (!j1.equals("?:\"?\"")) {
            //取出最深层原子
            String a1 = j1.split("\\{", -1)[j1.split("\\{", -1).length - 1].split("}", -1)[0];
            String a2 = j2.split("\\{", -1)[j2.split("\\{", -1).length - 1].split("}", -1)[0];
            String j2_ = j2;
            String a2_ = a2;
            //转换成原子项
            String i1[] = a1.split(",");
            //在同级原子中寻找相同的原子
            while (!a2.startsWith(",") && !a2.endsWith(",") && !a2.contains(":,") && !a2.contains(",,")) {
                //遍历消除
                for (String s : i1) {
                    a2_ = a2_.replace(s, "");
                }
                //此时a2_剩下的全是逗号，如果长度正好等于i1的长度-1，说明相等
                if (a2_.length() == i1.length - 1) {
                    //相等则从j1、j2中消除，消除不能简单的替换，因为其他位置可能有相同的结构，必须从当前位置上消除
                    int index = 0;
                    index = j1.lastIndexOf("{" + a1 + "}");
                    j1 = j1.substring(0, index) + j1.substring(index).replace("{" + a1 + "}", "?:\"?\"");
                    index = j2.lastIndexOf("{" + a2 + "}");
                    j2 = j2.substring(0, index) + j2.substring(index).replace("{" + a2 + "}", "?:\"?\"");
                    //递归
                    return compareAtom(j1, j2);
                } else {
                    //寻找下一个同级原子
                    j2_ = j2_.replace("{" + a2 + "}", "");
                    a2 = j2_.split("\\{", -1)[j2_.split("\\{", -1).length - 1].split("}", -1)[0];
                    a2_ = a2;
                }
            }
            return false;
        } else {
            //比较是否相同
            return j1.equals(j2);
        }
    }

    /**
     * json字符串特殊字符清理辅助方法
     * @param j       需要清理的json字符串
     * @param matcher 正则表达式匹配对象
     * @return 净化的json串
     */
    private static String cleanStr4Special(String j, Matcher matcher) {
        String group;
        String groupNew;
        while (matcher.find()) {
            group = matcher.group();
            groupNew = group.replaceAll("\\{", "A");
            groupNew = groupNew.replaceAll("}", "B");
            groupNew = groupNew.replaceAll(",", "C");
            j = j.replace(group, groupNew);
        }
        return j;
    }

    /**
     * json串字符串类型的value中的空格清理辅助方法
     * @param j       需要清理的json字符串
     * @param matcher 正则表达式匹配对象
     * @return 净化的json串
     */
    private static String cleanStr4Space(String j, Matcher matcher) {
        String group;
        String groupNew;
        while (matcher.find()) {
            group = matcher.group();
            groupNew = group.replaceAll(" ", "S");
            j = j.replace(group, groupNew);
        }
        return j;
    }

    /**
     * 用于断言录制、回放模式时把result写入param中
     * @param result   返回结果对象
     * @param context  入参excel集合
     * @param callback 回调函数
     * @throws Exception 异常
     */
    public static void resultCallBack(Object result, Map<String, Object> context, ITestResultCallback callback) throws Exception {
        // 把入参和执行结果写入param中
        callback.afterTestMethod(context, result, context.get(Constants.PARAMETER_NAME_));
    }

    /**
     * 异常处理
     * @param exception
     * @return
     */
    public static Result exceptionDeal(Exception exception) {
        String msg;
        if (exception instanceof InvocationTargetException) {
            msg = ExceptionUtils.getExceptionProfile(((InvocationTargetException) exception).getTargetException());
        } else {
            msg = ExceptionUtils.getExceptionProfile(exception);
        }
        return getResultObj(false, "500", msg);
    }

    private static Result getResultObj(Boolean success, String code, String message) {
        Result result = Result.create();
        result.setSuccess(success);
        result.setCode(code);
        result.setDescription(message);
        return result;
    }
}
