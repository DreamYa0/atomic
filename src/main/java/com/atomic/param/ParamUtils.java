package com.atomic.param;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.atomic.exception.ParameterException;
import com.atomic.exception.QueryDataException;
import com.atomic.param.entity.MethodMeta;
import com.atomic.util.DataSourceUtils;
import com.atomic.util.GsonUtils;
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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;


/**
 * excel参数处理工具类
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/9.
 */
public final class ParamUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParamUtils.class);

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
     * excel 支持sql语句，格式：sql:dataSourceName:select XXX
     * @param value
     * @param type
     * @return
     * @throws SQLException
     */
    public static String getSqlValue(String value, Type type) throws SQLException {
        // 格式：sql:dataBaseName:select XXX
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
     * 判断excel中的值是否为空
     * @param value
     * @return
     */
    public static boolean isExcelValueEmpty(Object value) {
        return value == null || org.apache.commons.lang3.StringUtils.isEmpty(value.toString()) ||
                Constants.EXCEL_NULL.equalsIgnoreCase(value.toString());
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
     * 把首字母转为小写
     * @param str
     * @return
     */
    public static String lowerFirst(String str) {
        if (Objects.isNull(str)||str.length()==0) {
            return str;
        }
        return str.replaceFirst(str.substring(0, 1), str.substring(0, 1).toLowerCase());
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
            if (BaseRequest.class.isAssignableFrom((Class<?>) rawType)) {
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
     * 获取Key对应Value，并转换成String
     * @param map
     * @param key
     * @return
     */
    public static String getString(Map<?, ?> map, Object key) {
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
                        jsonStr = GsonUtils.getGson().toJson(obj);
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
