package com.atomic.param;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.atomic.exception.ParameterException;
import com.atomic.param.assertcheck.AssertCheckUtils;
import com.atomic.param.entity.MethodMeta;
import com.atomic.param.parser.ExcelResolver;
import com.atomic.util.ReflectionUtils;
import com.g7.framework.common.dto.BaseRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.testng.Reporter;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * excel参数处理工具类
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/9.
 */
public final class ParamUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParamUtils.class);
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private ParamUtils() {
    }

    /**
     * 只有 1 或 Y 返回true
     * @param value
     * @return
     */
    public static boolean isValueTrue(Object value) {
        return value != null && ("1".equalsIgnoreCase(value.toString()) ||
                Constants.EXCEL_YES.equalsIgnoreCase(value.toString()));
    }

    /**
     * 是否期望结果为Y
     * @param param 入参
     * @return
     */
    public static boolean isExpectSuccess(Map<String, Object> param) {
        return isValueTrue(param.get(Constants.ASSERT_RESULT));
    }

    /**
     * 是否需要自动化测试
     * @param param 入参
     * @return
     */
    public static boolean isAutoTest(Map<String, Object> param) {
        return isValueTrue(param.get(Constants.AUTO_TEST));
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
        return !(param.get(Constants.HTTP_MODE) == null || "".equals(param.get(Constants.HTTP_MODE)));
    }

    /**
     * 当请求IP地址或域名不为空时返回true
     * @param param 入参
     * @return
     */
    public static boolean isHttpHostNoNull(Map<String, Object> param) {
        return !(param.get(Constants.HTTP_HOST) == null || "".equals(param.get(Constants.HTTP_HOST)));
    }

    /**
     * 当请求URI路径不为空时返回true
     * @param param 入参
     * @return
     */
    public static boolean isHttpMethodNoNull(Map<String, Object> param) {
        return !(param.get(Constants.HTTP_METHOD) == null || "".equals(param.get(Constants.HTTP_METHOD)));
    }

    public static boolean isContentTypeNoNull(Map<String, Object> param) {
        return !(param.get(Constants.CONTENT_TYPE) == null || "".equals(param.get(Constants.CONTENT_TYPE)));
    }

    public static boolean isHttpHeaderNoNull(Map<String, Object> param) {
        return param.get(Constants.HTTP_HEADER) != null;
    }

    public static boolean isLoginUrlNoNull(Map<String, Object> param) {
        return !(param.get(Constants.LOGIN_URL) == null || "".equals(param.get(Constants.LOGIN_URL)));
    }

    /**
     * 判断 excel 中是否有预期断言值
     * @param param excel 入参
     * @return 是否存在
     */
    public static boolean isExpectedResultNoNull(Map<String, Object> param) {
        return !(param.get(Constants.EXPECTED_RESULT) == null || "".equals(param.get(Constants.EXPECTED_RESULT)));
    }

    /**
     * excel入参中是否包含sid参数
     * @param param 入参 map 集合
     * @return 是否有
     */
    public static boolean isSidParam(Map<String, Object> param) {
        return param.get("sid") != null && !"".equals(param.get("sid"));
    }

    /**
     * excel入参中是否包含 actionInfo 参数
     * @param param 入参 map 集合
     * @return 是否有
     */
    public static boolean isActionInfoParam(Map<String, Object> param) {
        return param.get("actionInfo") != null && !"".equals(param.get("actionInfo"));
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
            throw new RuntimeException(String.format("size is %s, index is %s",
                    methodMeta.getParamTypes().length, index));
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
        newParam.remove(Constants.ASSERT_CODE);
        newParam.remove(Constants.ASSERT_MSG);
        newParam.remove(Constants.EXPECTED_RESULT);
        newParam.remove(Constants.TEST_ONLY);
        // newParam.remove(ASSERT_RESULT);
        newParam.remove(Constants.AUTO_TEST);
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
    public static Object[] generateParametersNew(final MethodMeta methodMeta,
                                                 Map<String, Object> newParam) throws Exception {
        final Object[] parameters = new Object[methodMeta.getParamTypes().length];
        for (int i = 0; i < methodMeta.getParamTypes().length; i++) {
            Type type = methodMeta.getParamTypes()[i];
            parameters[i] = generateParametersNew(type, newParam, getParamName(methodMeta, i));
        }
        return parameters;
    }

    private static Object generateParametersNew(Type type,
                                                Map<String, Object> param,
                                                String paramName) throws Exception {
        // 类，如：方法xxxx(Request)
        if (type instanceof Class) {
            return generateParametersNew(param, (Class) type, paramName);
        } else {
            // 泛型，取出泛型对象中的参数，如：方法xxxx(Request<T>)中的T
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
    private static Object generateParametersNew(Map<String, Object> param,
                                                Class<?> clazz,
                                                String paramName) throws Exception {

        if (clazz.equals(HttpSession.class)) {

            // 如接口请求参数中，存在HttpSession时，如Controller层入参可能存在HttpSession
            return param.get(Constants.HTTP_SESSION);

        } else if (clazz.equals(HttpServletRequest.class)) {

            // 如接口请求参数中，存在HttpServletRequest时，如Controller层入参可能存在HttpServletRequest
            return param.get(Constants.HTTP_SERVLET_REQUEST);

        } else if (clazz.equals(HttpServletResponse.class)) {

            // 如接口请求参数中，存在HttpServletResponse时，如Controller层入参可能存在HttpServletResponse
            return param.get(Constants.HTTP_SERVLET_RESPONSE);

        } else if (clazz.isInterface()) {

            // 暂时不会出现接口入参为接口的情况，暂不处理

        }

        Object value;
        // 基础类和包装类
        if (StringUtils.isBasicType(clazz)) {
            value = StringUtils.json2Bean(clazz.getSimpleName(), StringUtils.getValue(param.get(paramName)), clazz);
        } else if (clazz.isEnum()) {
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
    private static Object generateParametersNew(Type paramType,
                                                Type parameterizedType,
                                                Map<String, Object> param,
                                                String paramName) throws Exception {

        Class<?> requestClass = ((ParameterizedTypeImpl) paramType).getRawType();
        Object request;
        Object data;
        if (requestClass.isInterface()) {
            request = JSON.parseObject(getString(param, paramName), paramType);
        } else {
            request = ReflectionUtils.initFromClass(requestClass);

            // 为请求入参对象公共属性设置值
            generateCommonParameters(request, param);

            if (parameterizedType instanceof ParameterizedType) {
                data = JSON.parseObject(getString(param, paramName), parameterizedType);
            } else {
                Class<?> paramClass = (Class<?>) parameterizedType;
                if (StringUtils.isBasicType(paramClass)) {
                    data = StringUtils.json2Bean(paramClass.getSimpleName(), getString(param, paramName), parameterizedType);
                    setRequestData(request, data);
                    return request;
                } else {

                    if (param.containsKey("data")) {
                        // 当Excel中data字段值为Json时
                        String dataJson = param.get("data").toString();
                        data = StringUtils.json2Bean(new Gson(), dataJson, paramClass);
                    } else {
                        // 设置属性值
                        data = ReflectionUtils.initFromClass(paramClass);
                        StringUtils.transferMap2Bean(data, param);
                    }
                }
            }
            // 设置属性值
            setRequestData(request, data);
        }
        return request;
    }

    /**
     * 构造入参请求对象公共属性的值
     * @param request 入参Request或者BaseRequest对象
     * @param param   入参map集合
     */
    private static void generateCommonParameters(Object request, Map<String, Object> param) {

        try {
            // 获取类中以及父类中的所有属性
            List<Field> fields = ReflectionUtils.getAllFieldsList(request.getClass());

            // 必须要排除data 因为data为泛型，否则field.getGenericType()会报错
            List<Field> collect = filterFields(fields);

            for (Field field : collect) {

                // 公共属性为基本类型
                String fieldName = field.getName();
                Type fieldType = field.getGenericType();

                if (fieldType instanceof TypeVariable) {
                    // 如果 Field 为类型变量，则跳过
                    continue;
                }

                if (param.containsKey(fieldName)) {
                    // Excel中包含 公共属性为自定义对象或基本类型字段的值
                    String fieldValue = param.get(fieldName).toString();

                    if (org.apache.commons.lang3.StringUtils.isNoneEmpty(fieldValue)) {

                        try {
                            // 如果excel包含属性字段名称，默认excel中的值为Json格式
                            Object actualValue = StringUtils.json2Bean(fieldName, fieldValue, fieldType);

                            // 按照正常情况是根据set方法来设值，但是为了兼容lombok插件采用这种不是很优雅的方式
                            field.setAccessible(true);
                            field.set(request, actualValue);
                        } catch (Exception e) {
                            logger.error("给请求入参对象公共自定义对象属性设值失败，属性名称为：{}，对应设置的值为：{}",
                                    fieldName, fieldValue, e);
                        }
                    }


                } else if (Boolean.FALSE.equals(StringUtils.isBasicType((Class) fieldType))) {
                    // 采用excel多sheet进行设计,且字段为自定义对象
                    // 实例化field所表示的对象
                    Object commonObj = ReflectionUtils.initFromClass((Class) field.getGenericType());

                    try {
                        Object object = param.get(Constants.TESTMETHODMETA);
                        MethodMeta methodMeta = (MethodMeta) object;

                        Class testClass = methodMeta.getTestClass();
                        String className = testClass.getSimpleName();
                        String resource = testClass.getResource("").getPath();
                        String filePath = resource + className + ".xls";
                        String sheetName = field.getName();
                        ExcelResolver excel = new ExcelResolver(filePath,sheetName);
                        List<Map<String, Object>> sheetParams = excel.readDataByRow();

                        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(sheetParams))) {

                            Map<String, Object> sheetParam = sheetParams.get(
                                    Integer.parseInt(param.get(Constants.CASE_INDEX).toString()) - 1);

                            Object testObj = ReflectionUtils.initFromClass(methodMeta.getTestClass());
                            AssertCheckUtils.getDataBeforeTest(sheetParam, testObj);

                            sheetParam.putIfAbsent(Constants.TESTMETHODMETA, methodMeta);
                            StringUtils.transferMap2Bean(commonObj, sheetParam);
                        } else {

                            // 如果MethodMeta、Sheet不存在，则在默认sheet页中获取commonObj对象各个字段的值
                            StringUtils.transferMap2Bean(commonObj, param);
                        }
                    } catch (Exception e) {
                        // 如果MethodMeta、Sheet不存在，则按照原逻辑处理
                        StringUtils.transferMap2Bean(commonObj, param);
                    }

                    field.setAccessible(true);
                    field.set(request, commonObj);
                }
            }
        } catch (Exception e) {
            logger.error("给请求入参对象公共属性设值失败！", e);
        }
    }

    /**
     * 过滤实体中的公共参数
     * @param fields 属性集合
     * @return 过滤后的属性集合
     */
    public static List<Field> filterFields(List<Field> fields) {
        return fields.stream()
                .filter(field -> Boolean.FALSE.equals(field.getName().equals("serialVersionUID")
                        || field.getName().equals("data")
                        || field.getName().equals("log")))
                .collect(Collectors.toList());
    }

    /**
     * 获取Key对应Value，并转换成String
     * @param map
     * @param key
     * @return
     */
    private static String getString(Map<?, ?> map, Object key) {
        if (map.get(key) == null || "".equals(map.get(key).toString())) {
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
     * 判断excel测试用例中是否存在dependencyIndex字段
     * @param param 入参
     * @return
     */
    public static boolean isDependencyIndexNoNull(Map<String, Object> param) {
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
                    Gson gson = new GsonBuilder().setDateFormat(dateFormat).setPrettyPrinting().create();
                    try {
                        jsonStr = gson.toJson(obj);
                    } catch (Exception e) {
                        jsonStr = JSON.toJSONStringWithDateFormat(obj, dateFormat, SerializerFeature.PrettyFormat);
                    }
                    return jsonStr;
                } else {
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
        Assert.notNull(context, "入参字段不能为空！");
        if (!context.isEmpty()) {
            List<String> removeKey = getRemoveKey();
            Map<String, Object> inParam = Maps.newHashMap(context);
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
            Reporter.log(" 入参字段不能为空！");
            throw new ParameterException("入参字段不能为空！");
        }
    }

    /**
     * 非Http请求入参字段清单,移除框架关键字
     */
    private static List<String> getRemoveKey() {
        List<String> removeKey = new ArrayList<>();
        removeKey.add(Constants.CASE_NAME);
        // removeKey.add(Constants.CASE_INDEX);
        removeKey.add(Constants.ASSERT_RESULT);
        removeKey.add(Constants.TEST_ONLY);
        removeKey.add(Constants.ASSERT_CODE);
        removeKey.add(Constants.ASSERT_MSG);
        removeKey.add(Constants.EXPECTED_RESULT);
        removeKey.add(Constants.LOGIN_URL);
        removeKey.add(Constants.HTTP_MODE);
        removeKey.add(Constants.HTTP_HOST);
        removeKey.add(Constants.HTTP_METHOD);
        removeKey.add(Constants.CONTENT_TYPE);
        removeKey.add(Constants.HTTP_HEADER);
        removeKey.add(Constants.HTTP_ENTITY);
        removeKey.add(Constants.AUTO_ASSERT);
        removeKey.add(Constants.MESSAGE_TYPE);
        return removeKey;
    }

    /**
     * Http接口入参字段检查
     * @param context excel入参
     * @return
     */
    public static void checkKeyWord(Map<String, Object> context) {
        if (!ParamUtils.isHttpModeNoNull(context)) {
            Reporter.log("接口请求类型不能为空！例如：POST、GET！");
            throw new ParameterException("接口请求类型不能为空！例如：POST、GET！");
        }
        if (!ParamUtils.isHttpMethodNoNull(context)) {
            Reporter.log("接口请求 URI 路径不能为空！");
            throw new ParameterException("接口请求 URI 路径不能为空！");
        }
    }
}
