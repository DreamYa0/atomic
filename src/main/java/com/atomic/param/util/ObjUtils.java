package com.atomic.param.util;

import com.alibaba.fastjson.JSON;
import com.atomic.param.Constants;
import com.atomic.param.entity.MethodMeta;
import com.atomic.param.excel.parser.ExcelResolver;
import com.atomic.util.GsonUtils;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/9.
 */
public final class ObjUtils {

    private static final Logger logger = LoggerFactory.getLogger(ObjUtils.class);

    private ObjUtils() {

    }

    /**
     * 获取第一个入参
     *
     * @param methodMeta 元数据
     * @param param      excel 入参
     * @param <T>        类型
     * @return 实例
     */
    public static <T> T getParameter(MethodMeta methodMeta, Map<String, Object> param) {
        return getParameter(methodMeta, param, 0);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getParameter(MethodMeta methodMeta, Map<String, Object> param, int index) {
        if (index < 0 || index >= methodMeta.getParamTypes().length) {
            throw new RuntimeException(String.format("size is %s, index is %s",
                    methodMeta.getParamTypes().length, index));
        }
        return (T) param.get(Constants.PARAMETER_NAME_ + index);
    }

    /**
     * 使用map自动构造所有入参
     *
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
            parameters[i] = generateParametersNew(type, newParam, ParamUtils.getParamName(methodMeta, i));
        }
        return parameters;
    }

    private static Object generateParametersNew(Type type,
                                                Map<String, Object> param,
                                                String paramName) throws Exception {
        // 类，如：方法xxxx(Request)
        if (type instanceof Class) {
            return generateParametersNew(param, (Class<?>) type, paramName);
        } else {
            // 泛型，取出泛型对象中的参数，如：方法xxxx(Request<T>)中的T
            Type parameterizedType = ((ParameterizedType) type).getActualTypeArguments()[0];
            return generateParametersNew(type, parameterizedType, param, paramName);
        }
    }

    /**
     * 一般类的初始化
     *
     * @param param     数据
     * @param clazz     该入参类型
     * @param paramName 入参名称
     * @return 实例
     * @throws Exception 转换异常
     */
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
        if (isBasicType(clazz)) {
            value = json2Bean(clazz.getSimpleName(), getValue(param.get(paramName)), clazz);
        } else if (clazz.isEnum()) {
            // 枚举
            value = json2Bean(clazz.getSimpleName(), getValue(param.get(paramName)), clazz);
        } else {
            // 自定义对象
            value = ReflectionUtils.initFromClass(clazz);
            transferMap2Bean(value, param);
        }
        return value;
    }

    /**
     * 类似 Request<Integer>, Request<DTO>, PagedRequest<DTO> 型初始化
     *
     * @param paramType         入参类型
     * @param parameterizedType 参数化类型
     * @param param             入参
     * @param paramName         入参名称
     * @return 入参对象实例
     * @throws IllegalAccessException 转换异常
     */
    private static Object generateParametersNew(Type paramType,
                                                Type parameterizedType,
                                                Map<String, Object> param,
                                                String paramName) throws Exception {

        Class<?> requestClass = ((ParameterizedTypeImpl) paramType).getRawType();
        Object request;
        Object data;

        if (requestClass.isInterface()) {
            request = JSON.parseObject(ParamUtils.getString(param, paramName), paramType);
        } else {
            request = ReflectionUtils.initFromClass(requestClass);
            // 为请求入参对象公共属性设置值
            generateCommonParameters(request, param);

            if (parameterizedType instanceof ParameterizedType) {
                data = JSON.parseObject(ParamUtils.getString(param, paramName), parameterizedType);
            } else {
                Class<?> paramClass = (Class<?>) parameterizedType;
                if (isBasicType(paramClass)) {
                    data = json2Bean(paramClass.getSimpleName(), ParamUtils.getString(param, paramName),
                            parameterizedType);
                    setRequestData(request, data);
                    return request;
                } else {

                    if (param.containsKey("data")) {
                        // 当Excel中data字段值为Json时
                        String dataJson = param.get("data").toString();
                        data = json2Bean(GsonUtils.getGson(), dataJson, paramClass);
                    } else {
                        // 设置属性值
                        data = ReflectionUtils.initFromClass(paramClass);
                        transferMap2Bean(data, param);
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
     *
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

                Type fieldType = field.getGenericType();
                if (fieldType instanceof TypeVariable) {
                    // 如果 Field 为类型变量，则跳过
                    continue;
                }

                // 公共属性为基本类型
                String fieldName = field.getName();
                if (param.containsKey(fieldName)) {
                    // Excel中包含 公共属性为自定义对象或基本类型字段的值
                    String fieldValue = param.get(fieldName).toString();

                    if (StringUtils.isNoneEmpty(fieldValue)) {
                        try {
                            // 如果excel包含属性字段名称，默认excel中的值为Json格式
                            Object actualValue = json2Bean(fieldName, fieldValue, fieldType);
                            // 按照正常情况是根据set方法来设值，但是为了兼容lombok插件采用这种不是很优雅的方式
                            field.setAccessible(true);
                            field.set(request, actualValue);
                        } catch (Exception e) {
                            logger.error("给请求入参对象公共自定义对象属性设值失败，属性名称为：{}，对应设置的值为：{}",
                                    fieldName, fieldValue, e);
                        }
                    }


                } else if (Boolean.FALSE.equals(isBasicType((Class<?>) fieldType))) {
                    // 采用excel多sheet进行设计,且字段为自定义对象
                    // 实例化field所表示的对象
                    Object commonObj = ReflectionUtils.initFromClass((Class<?>) field.getGenericType());

                    try {
                        Object object = param.get(Constants.TESTMETHODMETA);
                        MethodMeta methodMeta = (MethodMeta) object;

                        Class<?> testClass = methodMeta.getTestClass();
                        String className = testClass.getSimpleName();
                        String resource = testClass.getResource("").getPath();
                        String filePath = resource + className + ".xls";
                        String sheetName = field.getName();
                        ExcelResolver excel = new ExcelResolver(filePath, sheetName);
                        List<Map<String, Object>> sheetParams = excel.readDataByRow();

                        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(sheetParams))) {

                            Map<String, Object> sheetParam = sheetParams.get(
                                    Integer.parseInt(param.get(Constants.CASE_INDEX).toString()) - 1);

                            Object testObj = ReflectionUtils.initFromClass(methodMeta.getTestClass());
                            ParamUtils.getDataBeforeTest(sheetParam, testObj);

                            sheetParam.putIfAbsent(Constants.TESTMETHODMETA, methodMeta);
                            transferMap2Bean(commonObj, sheetParam);
                        } else {

                            // 如果MethodMeta、Sheet不存在，则在默认sheet页中获取commonObj对象各个字段的值
                            transferMap2Bean(commonObj, param);
                        }
                    } catch (Exception e) {
                        // 如果MethodMeta、Sheet不存在，则按照原逻辑处理
                        transferMap2Bean(commonObj, param);
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
     * set属性的值到Bean，不支持的类型没有赋值
     *
     * @param bean   Request<T>中的T
     * @param valMap excel入参map集合
     */
    private static void transferMap2Bean(Object bean, Map<String, Object> valMap) {

        Class<?> cls = bean.getClass();
        List<Field> fields = new ArrayList<>();
        // 获取所有属性，包括继承的
        ReflectionUtils.getAllFields(cls, fields);
        // 必须要排除data 因为data为泛型，否则field.getGenericType()会报错
        List<Field> collect = filterFields(fields);

        for (Field field : collect) {
            try {

                Object obj = valMap.get(field.getName());
                Type genericType = field.getGenericType();

                if (genericType instanceof TypeVariable) {
                    // 如果 Field 为类型变量，则跳过
                    continue;
                }

                if (obj == null || ParamUtils.isExcelValueEmpty(obj.toString())) {
                    // 默认excel sheet中没有对应此属性的值或属性值为""

                    // 如果是参数化类型则跳过
                    if (genericType instanceof ParameterizedType) {
                        continue;
                    }

                    // 如果是基本类型也跳过
                    if (isBasicType((Class<?>) genericType)) {
                        // 基本类型
                        continue;
                    }

                    // 如是枚举类型也跳过
                    if (((Class<?>) genericType).isEnum()) {
                        continue;
                    }

                    // 自定义对象,且excel中未有对应字段的值或属性值为""，则采用excel多sheet进行设计
                    // 实例化自定义对象
                    Object fieldObj = ReflectionUtils.initFromClass((Class<?>) genericType);

                    try {
                        Object object = valMap.get(Constants.TESTMETHODMETA);
                        MethodMeta methodMeta = (MethodMeta) object;

                        Class<?> testClass = methodMeta.getTestClass();
                        String className = testClass.getSimpleName();
                        String resource = testClass.getResource("").getPath();
                        String filePath = resource + className + ".xls";

                        String sheetName = field.getName();
                        ExcelResolver excel = new ExcelResolver(filePath, sheetName);
                        List<Map<String, Object>> sheetParams = excel.readDataByRow();

                        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(sheetParams))) {
                            Map<String, Object> sheetParam = sheetParams.get(
                                    Integer.parseInt(valMap.get(Constants.CASE_INDEX).toString()) - 1);

                            Object testObj = ReflectionUtils.initFromClass(methodMeta.getTestClass());
                            ParamUtils.getDataBeforeTest(sheetParam, testObj);

                            sheetParam.putIfAbsent(Constants.TESTMETHODMETA, methodMeta);
                            transferMap2Bean(fieldObj, sheetParam);
                            setFieldValue(bean, field, fieldObj);
                        }

                    } catch (Exception e) {
                        // 如果MethodMeta、Sheet不存在，则按照原逻辑处理,从默认sheet页中获取值来进行设置
                        transferMap2Bean(fieldObj, valMap);
                        setFieldValue(bean, field, fieldObj);
                    }

                } else {
                    // 如果obj为 beforeTest方法里面直接放入的对象时
                    Object object;
                    if (Boolean.FALSE.equals(isBasicType(obj.getClass()))) {
                        object = obj;
                    } else {
                        String value = obj.toString();
                        String fieldType = field.getType().getSimpleName();
                        object = json2Bean(fieldType, value, genericType);
                    }

                    /*String fieldSetName = parSetName(field.getName(), fieldType);
                    String fieldIsSetName = parIsSetName(field.getName());
                    Method fieldSetMet;
                    if (getMethod(cls, fieldSetName) != null) {
                        fieldSetMet = getMethod(cls, fieldSetName);
                    } else if (getMethod(cls, fieldIsSetName) != null) {
                        fieldSetMet = getMethod(cls, fieldIsSetName);
                    } else {
                        continue;
                    }
                    fieldSetMet.invoke(bean, object);*/

                    // 由于普遍使用lombok注解来生成getter、setter方法，故使用此方式给属性设值，而不采用setter方法来设值
                    setFieldValue(bean, field, object);
                }
            } catch (Exception e) {
                Reporter.log("excel中的值转化为入参对象值异常！", true);
                Reporter.log(String.format("transferMap2Bean error, field is %s, value is %s ",
                        field.getName(), valMap.get(field.getName())) + e.getMessage());
                e.printStackTrace();
                //continue;
                // 把异常放入参里面去，打印出来，提醒用户数据有问题
                if (valMap.get(Constants.CASE_NAME) == null) {
                    String msg = getValue(valMap.get(Constants.EXCEL_DESC));
                    if (msg == null) {
                        msg = String.format(" 【属性转化异常】, field is %s, value is %s ",
                                field.getName(), valMap.get(field.getName()));
                    } else {
                        msg += String.format(" 【属性转化异常】, field is %s, value is %s ",
                                field.getName(), valMap.get(field.getName()));
                    }
                    valMap.put(Constants.EXCEL_DESC, msg);
                } else {
                    String msg = getValue(valMap.get(Constants.CASE_NAME));
                    if (msg == null) {
                        msg = String.format(" 【属性转化异常】, field is %s, value is %s ",
                                field.getName(), valMap.get(field.getName()));
                    } else {
                        msg += String.format(" 【属性转化异常】, field is %s, value is %s ",
                                field.getName(), valMap.get(field.getName()));
                    }
                    valMap.put(Constants.CASE_NAME, msg);
                }
            }
        }
    }

    /**
     * Json转JavaBean
     *
     * @param fieldType 字段类型
     * @param value     json 字符串
     * @param type      类型
     * @return 实例
     * @throws Exception 序列化异常
     */
    @SuppressWarnings("unchecked")
    public static Object json2Bean(String fieldType, String value, Type type) throws Exception {
        if (ParamUtils.isExcelValueEmpty(value)) {
            return null;
        }
        if ("String".equals(fieldType)) {
            return value;
        } else if ("Date".equals(fieldType)) {
            return parseDate(value);
        } else if (type instanceof Class && ((Class<?>) type).isEnum()) {
            //增加对枚举的处理

            return Enum.valueOf((Class) type, value);
        } else {
            // 支持sql语句
            // value = getSqlValue(value, type);

            // 按照json解析
            // fastjson不能反序列化，如：en-SB等类型的字符串
            // return JSON.parseObject(value, type);
            return GsonUtils.getGson().fromJson(value, type);
        }
    }

    /**
     * Json转JavaBean
     *
     * @param value     json 字符串
     * @param paramType 入参类型
     * @param paramName 入参名称
     * @return 实例
     */
    public static Object json2Bean(String value, Type paramType, String paramName) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(value))
            return null;
        try {
            // 支持sql语句
            value = ParamUtils.getSqlValue(value, paramType);
            return json2Bean(value, paramType);
        } catch (Exception e) {
            String error = String.format("参数赋值失败，param name is %s, value is %s", paramName, value);
            Reporter.log(error, true);
            return null;
        }
    }

    public static <T> T json2Bean(Gson gson, String jsonString, Class<T> type) {
        // 反序列化成Bean
        return gson.fromJson(jsonString, type);
    }

    private static <T> T json2Bean(String jsonString, Type type) {
        return json2Bean(GsonUtils.getGson(), jsonString, type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T json2Bean(Gson gson, String jsonString, Type type) {
        T t;
        if (type instanceof Class) {
            t = json2Bean(gson, jsonString, (Class<T>) type);
        } else {
            t = gson.fromJson(jsonString, type);
        }
        return t;
    }

    private static void setRequestData(Object request, Object data) throws Exception {
        // 为Request中的data字段注入值
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

    public static boolean isBasicType(Class<?> clz) {
        // 是否基础类或者其包装类，String、Date类型
        try {
            return clz.isPrimitive() || Arrays.asList("String", "Date", "BigDecimal").contains(clz.getSimpleName()) ||
                    ((Class<?>) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBasicType(String type) {
        // 是否基础类或者其包装类，String、Date类型
        // char 没判断
        return Arrays.asList("String", "Date", "Integer", "int", "Long", "long", "Double", "double", "Boolean",
                "boolean", "Byte", "byte", "BigDecimal", "Short", "short", "Float", "float").contains(type);
    }

    private static Date parseDate(String dateStr) {
        // 格式化string为Date
        if (ParamUtils.isExcelValueEmpty(dateStr)) {
            return null;
        }
        try {
            String fmtStr;
            if (dateStr.indexOf(':') > 0) {
                fmtStr = "yyyy-MM-dd HH:mm:ss";
            } else {
                fmtStr = "yyyy-MM-dd";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(fmtStr);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            Reporter.log("格式化String为Date异常！");
            return null;
        }
    }

    private static void setFieldValue(Object bean, Field field, Object fieldObj) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(bean, fieldObj);
    }

    private static List<Field> filterFields(List<Field> fields) {
        // 过滤实体中的公共参数
        return fields.stream()
                .filter(field -> Boolean.FALSE.equals(field.getName().equals("serialVersionUID")
                        || field.getName().equals("data")
                        || field.getName().equals("log")))
                .collect(Collectors.toList());
    }

    private static Method getMethod(Class<?> clazz,
                                    String methodName,
                                    final Class<?>... parameterTypes) throws Exception {
        // 在指定Class对象中获取指定方法
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() == null) {
                throw new NoSuchMethodException(String.format("方法[%s]不存在！", methodName));
            } else {
                method = getMethod(clazz.getSuperclass(), methodName, parameterTypes);
            }
        }
        return method;
    }

    public static String getValue(Object object) {
        if (object == null) {
            return null;
        } else {
            return object.toString();
        }
    }

    public static ForEachClass getForEachClass(Object object) throws Exception {
        // 解析测试用例中的for循环字段
        if (object == null)
            return null;
        String splits = object.toString();
        if (!splits.startsWith(Constants.EXCEL_FOREACH))
            return null;
        splits = splits.replace(Constants.EXCEL_FOREACH, "");
        String[] list = splits.split(":");
        if (list.length != 2) {
            Reporter.log("参数格式错误！, 格式示例：foreach0:100");
            throw new Exception("--------------参数格式错误！, 格式示例：foreach0:100 ---------------");
        }
        ForEachClass forEachClass = new ForEachClass();
        forEachClass.setStart(Integer.parseInt(list[0]));
        forEachClass.setEnd(Integer.parseInt(list[1]));
        return forEachClass;
    }

    public static class ForEachClass {
        // 解析测试用例中的for循环字段
        private int start;
        private int end;

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

    }
}
