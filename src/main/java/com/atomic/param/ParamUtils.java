package com.atomic.param;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.atomic.annotations.AnnotationUtils;
import com.atomic.exception.ParameterException;
import com.atomic.param.entity.MethodMeta;
import com.atomic.util.ReflectionUtils;
import com.atomic.util.SaveResultUtils;
import com.coinsuper.common.model.BaseRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.util.Args;
import org.testng.ITestResult;
import org.testng.Reporter;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * excel参数处理工具类
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/9.
 */
public final class ParamUtils {

    private ParamUtils() {
    }

    /**
     * 只有 1 或 Y 返回true
     * @param value
     * @return
     */
    public static boolean isValueTrue(Object value) {
        if (value == null) {
            return false;
        }
        return "1".equalsIgnoreCase(value.toString()) || Constants.EXCEL_YES.equalsIgnoreCase(value.toString());
    }

    /**
     * 是否期望结果为Y
     * @param param 入参
     * @return
     */
    public static boolean isExpectSuccess(Map<String, Object> param) {
        return isValueTrue(param.get(Constants.ASSERTRESULT_NAME));
    }

    /**
     * 是否需要自动化测试
     * @param param 入参
     * @return
     */
    public static boolean isAutoTest(Map<String, Object> param) {
        return isValueTrue(param.get(Constants.AUTOTEST_NAME));
    }

    /**
     * 判断excel中是否存在autoAssert字段
     * @param param 入参
     * @return
     */
    public static boolean isAutoAssert(Map<String, Object> param) {
        return isValueTrue(param.get(Constants.AUTO_ASSERT));
    }

    /**
     * 当请求类型不为空是返回true
     * @param param 入参
     * @return
     */
    public static boolean isHttpModeNoNull(Map<String, Object> param) {
        if (param.get(Constants.HTTP_MODE) == null || "".equals(param.get(Constants.HTTP_MODE))) {
            return false;
        }
        return true;
    }

    /**
     * 当请求IP地址或域名不为空时返回true
     * @param param 入参
     * @return
     */
    public static boolean isHttpHostNoNull(Map<String, Object> param) {
        if (param.get(Constants.HTTP_HOST) == null || "".equals(param.get(Constants.HTTP_HOST))) {
            return false;
        }
        return true;
    }

    /**
     * 当请求URI路径不为空时返回true
     * @param param 入参
     * @return
     */
    public static boolean isHttpMethodNoNull(Map<String, Object> param) {
        if (param.get(Constants.HTTP_METHOD) == null || "".equals(param.get(Constants.HTTP_METHOD))) {
            return false;
        }
        return true;
    }

    public static boolean isHttpContentTypeNoNull(Map<String, Object> param) {
        if (param.get(Constants.HTTP_CONTENT_TYPE) == null || "".equals(param.get(Constants.HTTP_CONTENT_TYPE))) {
            return false;
        }
        return true;
    }

    public static boolean isHttpHeaderNoNull(Map<String, Object> param) {
        if (param.get(Constants.HTTP_HEADER) == null) {
            return false;
        }
        return true;
    }

    public static boolean isLoginUrlNoNull(Map<String, Object> param) {
        if (param.get(Constants.LOGIN_URL) == null || "".equals(param.get(Constants.LOGIN_URL))) {
            return false;
        }
        return true;
    }

    /**
     * 判断 excel 中是否有预期断言值
     * @param param excel 入参
     * @return 是否存在
     */
    public static boolean isExpectedResultNoNull(Map<String, Object> param) {
        if (param.get(Constants.EXPECTED_RESULT) == null || "".equals(param.get(Constants.EXPECTED_RESULT))) {
            return false;
        }
        return true;
    }

    /**
     * excel入参中是否包含sid参数
     * @param param 入参 map 集合
     * @return 是否有
     */
    public static boolean isSidParam(Map<String, Object> param) {
        if (param.get("sid") != null && !"".equals(param.get("sid"))) {
            return true;
        }
        return false;
    }

    /**
     * excel入参中是否包含 actionInfo 参数
     * @param param 入参 map 集合
     * @return 是否有
     */
    public static boolean isActionInfoParam(Map<String, Object> param) {
        if (param.get("actionInfo") != null && !"".equals(param.get("actionInfo"))) {
            return true;
        }
        return false;
    }

    /**
     * 获取参数名称
     * @param methodMeta 测试方法信息
     * @param index      序号
     * @return
     */
    public static String getParamName(MethodMeta methodMeta, int index) {
        if (methodMeta.getParamTypes().length == 1) {
            return methodMeta.getMultiTimeField();
        } else {
            // 多个入参，其中一个入参类似Request<Integer>
            return methodMeta.getParamNames()[index];
        }
    }

    /**
     * 获取入参
     * @param testResult 测试结果视图
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getParamContext(ITestResult testResult) {
        return (Map<String, Object>) testResult.getParameters()[0];
    }

    /**
     * 获取第一个入参
     * @param param 入参
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T getParameter(MethodMeta methodMeta, Map<String, Object> param) {
        return getParameter(methodMeta, param, 0);
    }

    /**
     * 获取某个入参
     * @param param 入参
     * @param index 参数位置索引
     * @param <T>
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static <T> T getParameter(MethodMeta methodMeta, Map<String, Object> param, int index) {
        if (index < 0 || index >= methodMeta.getParamTypes().length) {
            throw new RuntimeException(String.format("size is %s, index is %s", methodMeta.getParamTypes().length, index));
        }
        return (T) param.get(Constants.PARAMETER_NAME_ + index);
    }

    /**
     * 判断入参是否是继承的 BaseRequest 类
     * @param method
     * @param paramIndex
     * @return
     * @throws Exception
     */
    public static boolean isParamTypeExtendsBaseRequest(Method method, int paramIndex) throws Exception {
        if (method.getGenericParameterTypes()[paramIndex] instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) (method.getGenericParameterTypes()[paramIndex])).getRawType();
            if (BaseRequest.class.isAssignableFrom((Class) rawType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 移除excel入参中的关键字字段
     * @param param excel入参
     * @return 集合
     */
    public static Map<String, Object> getParamContextWithoutExtraInfo(Map<String, Object> param) {
        Map<String, Object> newParam = Maps.newHashMap(param);
        newParam.remove(Constants.TESTMETHODMETA);
        // newParam.remove(PARAMETER_NAME_);
        // newParam.remove(RESULT_NAME);
        newParam.remove(Constants.ASSERTRESULT_CODE);
        newParam.remove(Constants.ASSERTRESULT_MSG);
        newParam.remove(Constants.EXPECTED_RESULT);
        newParam.remove(Constants.TEST_ONLY);
        // newParam.remove(ASSERTRESULT_NAME);
        newParam.remove(Constants.AUTOTEST_NAME);
        List<String> keys = Lists.newArrayList(param.keySet());
        keys.stream().filter(key -> key.startsWith(Constants.ASSERT_ITEM_)).forEach(param::remove);
        return newParam;
    }

    /**
     * 使用map自动构造所有入参
     * @param methodMeta 属性
     * @param newParam   excel入参
     * @return 入参对象
     * @throws Exception 异常
     */
    public static Object[] generateParametersNew(final MethodMeta methodMeta, Map<String, Object> newParam) throws Exception {
        final Object[] parameters = new Object[methodMeta.getParamTypes().length];
        for (int i = 0; i < methodMeta.getParamTypes().length; i++) {
            parameters[i] = generateParametersNew(methodMeta.getParamTypes()[i], newParam, getParamName(methodMeta, i));
        }
        return parameters;
    }

    private static Object generateParametersNew(Type type, Map<String, Object> param, String paramName) throws Exception {
        // 类
        if (type instanceof Class) {
            return generateParametersNew(param, (Class) type, paramName);
        } else {
            // 泛型
            Type parameterizedType = ((ParameterizedType) type).getActualTypeArguments()[0];
            return generateParametersNew(type, parameterizedType, param, paramName);
        }
    }

    /**
     * 一般类的初始化
     * @param param     数据
     * @param clazz     该入参类型
     * @param paramName 入参名称
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static Object generateParametersNew(Map<String, Object> param, Class clazz, String paramName) throws Exception {
        Object value;
        // 基础类和包装类
        if (StringUtils.isBasicType(clazz)) {
            value = StringUtils.json2Bean(clazz.getSimpleName(), StringUtils.getValue(param.get(paramName)), clazz);
        } else {
            // 复杂类
            value = ReflectionUtils.initFromClass(clazz);
            StringUtils.transferMap2Bean(value, param);
        }
        return value;
    }

    /**
     * 类似 Request<Integer>, Request<DTO>, XXXRequest<DTO> 型初始化
     * @param paramType
     * @param parameterizedType
     * @param param
     * @param paramName
     * @return
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    private static Object generateParametersNew(Type paramType, Type parameterizedType, Map<String, Object> param, String paramName) throws Exception {
        Class requestClass = ((ParameterizedTypeImpl) paramType).getRawType();
        Object request;
        Object baseRequest;
        Object data;
        if (requestClass.isInterface()) {
            request = JSON.parseObject(getString(param, paramName), paramType);
        } else {
            request = ReflectionUtils.initFromClass(requestClass);
            if (parameterizedType instanceof ParameterizedType) {
                data = JSON.parseObject(getString(param, paramName), parameterizedType);
            } else {
                Class paramClass = (Class) parameterizedType;
                if (StringUtils.isBasicType(paramClass)) {
                    data = StringUtils.json2Bean(paramClass.getSimpleName(), getString(param, paramName), parameterizedType);
                    setRequestData(request, data);
                    if (isLanguageParam(param)) {
                        baseRequest = ReflectionUtils.initFromClass(request.getClass().getSuperclass());
                        Optional.ofNullable(ReflectionUtils.getField(baseRequest, Object.class, "language")).ifPresent(field -> {
                            field.setAccessible(true);
                            try {
                                field.set(request, param.get("language"));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        });
                    } /* else if (isSidParam(param)) {
                        baseRequest = ReflectionUtils.initFromClass(request.getClass().getSuperclass());
                        Optional.ofNullable(getField(baseRequest, Object.class, "sid")).ifPresent(field -> {
                            field.setAccessible(true);
                            try {
                                field.set(request,param.get("sid"));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        });
                    } *//* else if (isActionInfoParam(param)) {
                        baseRequest = ReflectionUtils.initFromClass(request.getClass().getSuperclass());
                        Optional.ofNullable(getField(baseRequest, Object.class, "actionInfo")).ifPresent(field -> {
                            Object actionInfo = JSON.parseObject(param.get("actionInfo").toString(), field.getType());
                            field.setAccessible(true);
                            try {
                                field.set(request,actionInfo);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        });
                    }*/
                    return request;
                } else {
                    data = ReflectionUtils.initFromClass(paramClass);
                    StringUtils.transferMap2Bean(data, param);// 设置属性值
                }
            }
            StringUtils.transferMap2Bean(request, param);// 设置属性值
            setRequestData(request, data);
        }
        return request;
    }

    /**
     * 获取Key对应Value，并转换成String
     * @param map
     * @param key
     * @return
     */
    private static String getString(Map<?, ?> map, Object key) {
        if (map.get(key) == null) {
            return null;
        } else {
            return map.get(key).toString();
        }
    }

    /**
     * excel入参中是否包含language参数
     * @param param 入参 map 集合
     * @return 是否有
     */
    private static boolean isLanguageParam(Map<String, Object> param) {
        if (param.get("language") != null && !"".equals(param.get("language"))) {
            return true;
        }
        return false;
    }

    /**
     * 为Request中的data字段注入值
     * @param request
     * @param data
     * @throws Exception
     */
    private static void setRequestData(Object request, Object data) throws Exception {
        // 查找泛型是T的属性，但是因为被擦除了，类型是Object
        Optional.ofNullable(ReflectionUtils.getField(request, Object.class, "data")).ifPresent(f -> {
            f.setAccessible(true);
            try {
                f.set(request, data);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 注入场景测试所需要的依赖方法的返回结果
     * @param iTestResult 测试结果上下问
     */
    public static void injectScenarioReturnResult(ITestResult iTestResult, Map<String, Object> context) {
        //获取接口所依赖的返回值并注入到context中
        String[] dependsOnMethodNames = AnnotationUtils.getDependsOnMethods(iTestResult);
        if (dependsOnMethodNames != null && dependsOnMethodNames.length > 0) {
            for (String dependsOnMethodName : dependsOnMethodNames) {
                Object dependMethodReturn;
                if (isDependencyIndexNoNull(context)) {
                    dependMethodReturn = SaveResultUtils.getTestResultInCache(dependsOnMethodName, context.get(Constants.DEPENDENCY_INDEX));
                } else {
                    dependMethodReturn = SaveResultUtils.getTestResultInCache(dependsOnMethodName, context.get(Constants.CASE_INDEX));
                }
                if (dependMethodReturn != null) {
                    context.put(dependsOnMethodName, dependMethodReturn);
                }
            }
        }
    }

    /**
     * 判断excel测试用例中是否存在dependencyIndex字段
     * @param param 入参
     * @return
     */
    private static boolean isDependencyIndexNoNull(Map<String, Object> param) {
        if (param.get(Constants.DEPENDENCY_INDEX) == null || "".equals(param.get(Constants.DEPENDENCY_INDEX))) {
            return false;
        }
        return true;
    }

    /**
     * 对象转json,并格式化输出
     * @param obj
     * @param prettyFormat
     * @return
     */
    public static String getJSONStringWithDateFormat(Object obj, boolean prettyFormat, String dateFormat) {
        try {
            if (org.apache.commons.lang3.StringUtils.isEmpty(dateFormat)) {
                return JSON.toJSONString(obj, prettyFormat);
            } else {
                String jsonStr;
                if (prettyFormat) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    try {
                        jsonStr = gson.toJson(obj);
                    } catch (Exception e) {
                        jsonStr = JSON.toJSONStringWithDateFormat(obj, dateFormat, SerializerFeature.PrettyFormat);
                    }
                    return jsonStr;
                } else {
                    Gson gson = new Gson();
                    try {
                        jsonStr = gson.toJson(obj);
                    } catch (Exception e) {
                        jsonStr = JSON.toJSONStringWithDateFormat(obj, dateFormat);
                    }
                    return jsonStr;
                }
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取请求需要的入参
     * @param context
     * @return
     */
    public static Map<String, Object> getParameters(Map<String, Object> context) {
        Args.notNull(context, "入参对象");
        if (!context.isEmpty()) {
            List<String> removeKey = getRemoveKey();
            Map<String, Object> inParam = new HashMap<>();
            inParam.putAll(context);
            Iterator<Map.Entry<String, Object>> iterMap = inParam.entrySet().iterator();
            while (iterMap.hasNext()) {
                Map.Entry<String, Object> entry = iterMap.next();
                // 通过迭代器移除非接口请求入参
                for (String key : removeKey) {
                    if (entry.getKey().contains(key)) {
                        iterMap.remove();
                        continue;
                    }
                }
            }
            return inParam;
        } else {
            Reporter.log(" 入参对象不能为空！");
            throw new ParameterException("入参对象不能为空！");
        }
    }

    /**
     * 非Http请求入参字段清单
     */
    private static List<String> getRemoveKey() {
        List<String> removeKey = new ArrayList<>();
        removeKey.add("caseName");
        removeKey.add("caseDescription");
        removeKey.add("caseRun");
        removeKey.add("caseType");
        removeKey.add("casePriority");
        removeKey.add("CASE_INDEX");
        removeKey.add("assertResult");
        removeKey.add("testOnly");
        removeKey.add("assertCode");
        removeKey.add("assertDescription");
        removeKey.add("expectedResult");
        removeKey.add("loginUrl");
        removeKey.add("httpMode");
        removeKey.add("httpHost");
        removeKey.add("httpMethod");
        removeKey.add("httpContentType");
        removeKey.add("httpHeader");
        removeKey.add("httpEntity");
        removeKey.add("autoAssert");
        removeKey.add("messageType");
        return removeKey;
    }
}
