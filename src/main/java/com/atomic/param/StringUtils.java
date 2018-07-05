package com.atomic.param;

import com.alibaba.fastjson.JSON;
import com.atomic.exception.QueryDataException;
import com.atomic.param.assertcheck.AssertCheckUtils;
import com.atomic.param.entity.MethodMeta;
import com.atomic.util.DataSourceUtils;
import com.atomic.util.ExcelUtils;
import com.atomic.util.ReflectionUtils;
import com.google.gson.Gson;
import org.springframework.util.CollectionUtils;
import org.testng.Reporter;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/9.
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * 是否基础类或者其包装类，String、Date类型
     * @param clz
     * @return
     */
    public static boolean isBasicType(Class clz) {
        try {
            return clz.isPrimitive() || Arrays.asList("String", "Date", "BigDecimal").contains(clz.getSimpleName()) || ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBasicType(String type) {
        // char 没判断
        return Arrays.asList("String", "Date", "Integer", "int", "Long", "long", "Double", "double", "Boolean", "boolean", "Byte"
                , "byte", "BigDecimal", "Short", "short", "Float", "float").contains(type);
    }

    /**
     * 判断excel中的值是否为空
     * @param value
     * @return
     */
    public static boolean isExcelValueEmpty(Object value) {
        return value == null || org.apache.commons.lang3.StringUtils.isEmpty(value.toString()) || Constants.EXCEL_NULL.equalsIgnoreCase(value.toString());
    }

    /**
     * Json转JavaBean
     * @param fieldType
     * @param value
     * @param type
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Object json2Bean(String fieldType, String value, Type type) throws Exception {
        if (isExcelValueEmpty(value)) {
            return null;
        }
        if ("String".equals(fieldType)) {
            return value;
        } else if ("Date".equals(fieldType)) {
            return parseDate(value);
        } else if (type instanceof Class && ((Class) type).isEnum()) {
            //增加对枚举的处理
            return Enum.valueOf((Class) type, value);
        } else {
            // 支持sql语句
            // value = getSqlValue(value, type);

            // 按照json解析
            // fastjson不能反序列化，如：en-SB等类型的字符串
            // return JSON.parseObject(value, type);
            Gson gson = new Gson();
            return gson.fromJson(value, type);
        }
    }

    /**
     * @param value
     * @param paramType
     * @param paramName
     * @return
     */
    public static Object json2Bean(String value, Type paramType, String paramName) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(value))
            return null;
        try {
            // 支持sql语句
            value = getSqlValue(value, paramType);
            return json2Bean(value, paramType);
        } catch (Exception e) {
            String error = String.format("参数赋值失败，param name is %s, value is %s", paramName, value);
            Reporter.log(error, true);
            return null;
        }
    }

    /**
     * 反序列化成Bean
     * @param jsonString
     * @param type
     * @param <T>
     * @return
     */
    private static <T> T json2Bean(String jsonString, Type type) {
        return json2Bean(new Gson(), jsonString, type);
    }

    /**
     * 反序列化成Bean
     * @param gson
     * @param jsonString
     * @param type
     * @param <T>
     * @return
     */
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

    /**
     * 反序列化成Bean
     * @param jsonString
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T json2Bean(Gson gson, String jsonString, Class<T> type) {
        return gson.fromJson(jsonString, type);
    }

    /**
     * 格式化string为Date
     * @param dateStr
     * @return date
     */
    private static Date parseDate(String dateStr) {
        if (isExcelValueEmpty(dateStr)) {
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

    /**
     * excel 支持sql语句，格式：sql:dataSourceName:select XXX
     * @param value
     * @param type
     * @return
     * @throws SQLException
     */
    public static String getSqlValue(String value, Type type) throws SQLException {
        // 格式：sql:dataSourceName:select XXX
        if (value.startsWith("sql:")) {
            value = value.substring(4);
            int index = value.indexOf(":");
            if (index > -1) {
                String dataSourceName = value.substring(0, index);
                String sql = value.substring(index + 1);
                List<Map<Integer, Object>> datalist = DataSourceUtils.queryData(dataSourceName, sql);
                value = handleDataList(datalist, value, type, sql);
            }
        }
        return value;
    }

    /**
     * 处理查询出的结果集
     * @param datalist
     * @param value
     * @param type
     * @param sql
     * @return
     */
    public static String handleDataList(List<Map<Integer, Object>> datalist, String value, Type type, String sql) {
        if (!CollectionUtils.isEmpty(datalist)) {
            if (type instanceof ParameterizedTypeImpl && ((ParameterizedTypeImpl) type).getRawType() == List.class) {
                List<String> list = new ArrayList<>(datalist.size());
                // 只要第一个字段
                list.addAll(datalist.stream().map(map -> String.valueOf(map.get(0))).collect(toList()));
                value = JSON.toJSONString(list);
            } else {
                value = String.valueOf(datalist.get(0).get(0));// 只要第一个字段
            }
        } else {
            Reporter.log("请检查sql语句格式是否有误或查询数据是否存在！sql：" + sql + "");
            throw new QueryDataException(String.format("请检查sql语句格式是否有误或查询数据是否存在！sql ：%s", sql));
        }
        return value;
    }

    /**
     * set属性的值到Bean，不支持的类型没有赋值
     * @param bean   Request<T>中的T
     * @param valMap excel入参map集合
     */
    protected static void transferMap2Bean(Object bean, Map<String, Object> valMap) {

        Class cls = bean.getClass();
        List<Field> fields = new ArrayList<>();
        // 获取所有属性，包括继承的
        ReflectionUtils.getAllFields(cls, fields);
        for (Field field : fields) {
            try {

                Object obj = valMap.get(field.getName());
                Type genericType = field.getGenericType();

                if (genericType instanceof TypeVariable) {
                    // 如果 Field 为类型变量，则跳过
                    continue;
                }

                if (obj == null || isExcelValueEmpty(obj.toString())) {
                    // 默认excel sheet中没有对应此属性的值或属性值为""

                    if (isBasicType((Class) genericType)) {
                        // 基本类型
                        continue;
                    } else {
                        // 自定义对象,且excel中未有对应字段的值或属性值为""，则采用excel多sheet进行设计

                        // 实例化自定义对象
                        Object fieldObj = ReflectionUtils.initFromClass((Class) genericType);

                        try {
                            Object object = valMap.get(Constants.TESTMETHODMETA);
                            MethodMeta methodMeta = (MethodMeta) object;

                            String sheetName = field.getName();
                            ExcelUtils excel = new ExcelUtils();
                            List<Map<String, Object>> sheetParams = excel.readDataByRow(methodMeta, sheetName);

                            if (Boolean.FALSE.equals(CollectionUtils.isEmpty(sheetParams))) {
                                Map<String, Object> sheetParam = sheetParams.get(Integer.valueOf(valMap.get(Constants.CASE_INDEX).toString()) - 1);

                                Object testObj = ReflectionUtils.initFromClass(methodMeta.getTestClass());
                                AssertCheckUtils.getDataBeforeTest(sheetParam, testObj);

                                sheetParam.putIfAbsent(Constants.TESTMETHODMETA, methodMeta);
                                transferMap2Bean(fieldObj, sheetParam);
                            }

                        } catch (Exception e) {
                            // 如果MethodMeta、Sheet不存在，则按照原逻辑处理,从默认sheet页中获取值来进行设置
                            transferMap2Bean(fieldObj, valMap);
                        }
                    }

                } else {

                    // 如果obj为 beforeTest方法里面直接放入的对象时
                    Object object;
                    if (Objects.equals(genericType, obj.getClass())) {

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
                    field.setAccessible(true);
                    field.set(bean, object);
                }
            } catch (Exception e) {
                Reporter.log("excel中的值转化为入参对象值异常！", true);
                Reporter.log(String.format("StringUtil#transferMap2Bean error, field is %s, value is %s ", field.getName(), valMap.get(field.getName())) + e);
                e.printStackTrace();
                //continue;
                // 把异常放入参里面去，打印出来，提醒用户数据有问题
                if (valMap.get(Constants.CASE_NAME) == null) {
                    String msg = getValue(valMap.get(Constants.EXCEL_DESC));
                    if (msg == null) {
                        msg = String.format(" 【属性转化异常】, field is %s, value is %s ", field.getName(), valMap.get(field.getName()));
                    } else {
                        msg += String.format(" 【属性转化异常】, field is %s, value is %s ", field.getName(), valMap.get(field.getName()));
                    }
                    valMap.put(Constants.EXCEL_DESC, msg);
                } else {
                    String msg = getValue(valMap.get(Constants.CASE_NAME));
                    if (msg == null) {
                        msg = String.format(" 【属性转化异常】, field is %s, value is %s ", field.getName(), valMap.get(field.getName()));
                    } else {
                        msg += String.format(" 【属性转化异常】, field is %s, value is %s ", field.getName(), valMap.get(field.getName()));
                    }
                    valMap.put(Constants.CASE_NAME, msg);
                }
            }
        }
    }

    /**
     * 在指定Class对象中获取指定方法
     * @param clazz          Class对象
     * @param methodName     方法名称
     * @param parameterTypes 入参Type
     * @return Method对象
     * @throws Exception .{@link Exception}
     */
    private static Method getMethod(Class<?> clazz, String methodName, final Class<?>... parameterTypes) throws Exception {
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

    /**
     * 把首字母转为小写
     * @param str
     * @return
     */
    public static String lowerFirst(String str) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(str)) {
            return str;
        }
        return str.replaceFirst(str.substring(0, 1), str.substring(0, 1).toLowerCase());
    }

    /**
     * 拼接boolean字段方法
     * @param fieldName
     * @return
     */
    public static String parIsSetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 拼接在某属性的 set方法
     * @param fieldName
     * @return String
     */
    public static String parSetName(String fieldName, String fieldType) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        if (("Boolean".equals(fieldType) || "boolean".equals(fieldType)) && fieldName.startsWith("is")) {
            fieldName = fieldName.substring(2);
        }
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 解析测试用例中的for循环字段
     * @param object
     * @return
     * @throws Exception
     */
    public static ForEachClass getForEachClass(Object object) throws Exception {
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
        forEachClass.setStart(Integer.valueOf(list[0]));
        forEachClass.setEnd(Integer.valueOf(list[1]));
        return forEachClass;
    }

    /**
     * 解析测试用例中的for循环字段
     * 嵌套类
     */
    public static class ForEachClass {
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
