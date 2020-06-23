package com.atomic.param;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.atomic.exception.ParameterException;
import com.atomic.exception.QueryDataException;
import com.atomic.param.entity.MethodMeta;
import com.atomic.param.excel.handler.DateParamHandler;
import com.atomic.param.excel.handler.EmailHandler;
import com.atomic.param.excel.handler.IHandler;
import com.atomic.param.excel.handler.IdCardHandler;
import com.atomic.param.excel.handler.PhoneNoHandler;
import com.atomic.param.excel.handler.RandomParamHandler;
import com.atomic.param.excel.parser.ExcelResolver;
import com.atomic.tools.assertcheck.AssertCheckUtils;
import com.atomic.tools.assertcheck.entity.AssertItem;
import com.atomic.tools.assertcheck.enums.AssertType;
import com.atomic.util.DataSourceUtils;
import com.atomic.util.GsonUtils;
import com.atomic.util.TestNGUtils;
import com.g7.framework.common.dto.BaseRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.testng.ITestResult;
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
import java.util.Set;

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

    public static void getDataBeforeTest(Map<String, Object> context, Object testInstance) throws Exception {
        // 断言前需要获取数据库的值
        List<AssertItem> assertItemList = AssertCheckUtils.getAssertItemList(testInstance, true);
        if (!CollectionUtils.isEmpty(assertItemList)) {
            for (int i = 0; i < assertItemList.size(); i++) {
                // 断言前获取数据
                if (assertItemList.get(i).getAssertType() == AssertType.OLD_VALUE_BEFORE_TEST.getCode()) {
                    context.put(Constants.ASSERT_ITEM_ + i, getOldValue(assertItemList.get(i), context));
                    if (org.apache.commons.lang3.StringUtils.isEmpty(assertItemList.get(i).getOldValue())) {
                        context.put(Constants.OLD_SQL_ + i, assertItemList.get(i).getOldSqlEntity().getSql());
                    }
                }
            }
        }
        // 把入参的 sql 设置为真实的值
        context.keySet().stream().filter(key -> context.get(key) != null).forEach(key -> {
            try {
                context.put(key, getRealValue(context, key));
                // context.put(key, randomParamValue(context.get(key)));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // handleUserKey(key, context);
        });

        IHandler randomParamHandler = new RandomParamHandler();
        IHandler cardHandler = new IdCardHandler();
        IHandler phoneNoHandler = new PhoneNoHandler();
        IHandler emailHandler = new EmailHandler();
        IHandler dateParamHandler = new DateParamHandler();

        randomParamHandler.setHandler(cardHandler);
        cardHandler.setHandler(phoneNoHandler);
        phoneNoHandler.setHandler(emailHandler);
        emailHandler.setHandler(dateParamHandler);
        randomParamHandler.handle(context);
    }

    private static Object getOldValue(AssertItem item, Map<String, Object> context) throws Exception {
        // 通过数据库获取断言前的值
        Object oldValue = item.getOldValue();
        if (org.apache.commons.lang3.StringUtils.isEmpty(item.getOldValue())) {
            oldValue = AssertCheckUtils.getSqlValue(item.getOldSqlEntity(), context);
        }
        return oldValue;
    }

    private static Object getRealValue(Map<String, Object> param, String field) throws SQLException {
        // 获取SQL语句对应的真实值
        Object value = param.get(field);
        if (value instanceof String) {
            value = ParamUtils.getSqlValue(value.toString(), String.class);
            // 把入参里面的 sql 语句改成真实的值
            param.put(field, value);
        }
        return value;
    }

    public static Map<String, Object> assemblyParamMap2RequestMap(ITestResult testResult,
                                                                  Object instance,
                                                                  Map<String, Object> context) {
        // 把excel入参中多个sheet，组合为真正入参的map集合
        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(context))) {
            Set<String> keys = context.keySet();
            for (String key : keys) {
                Object value = context.get(key);
                if (Objects.isNull(value) || "".equals(value)) {

                    String className = TestNGUtils.getTestCaseClassName(testResult);
                    String resource = instance.getClass().getResource("").getPath();
                    String filePath = resource + className + ".xls";

                    ExcelResolver excel = new ExcelResolver(filePath,key);
                    try {
                        List<Map<String, Object>> maps = excel.readDataByRow();
                        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(maps))) {
                            Map<String, Object> map = maps.get(Integer.parseInt(
                                    context.get(Constants.CASE_INDEX).toString()) - 1);
                            // 把excel中的值转换为真实值
                            getDataBeforeTest(map);
                            Map<String, Object> assemblyMap = assemblyParamMap2RequestMap(testResult, instance, map);
                            assemblyMap.remove(Constants.CASE_INDEX);
                            context.put(key, assemblyMap);
                        }
                    } catch (Exception e) {
                        // 如果Sheet不存在，则按照原逻辑处理
                    }
                }
            }
        }
        return context;
    }

    public static void getDataBeforeTest(Map<String, Object> param) {

        // 执行测试之前替换excel中的特殊字段为真实的值
        // 把入参的 sql 设置为真实的值
        param.forEach((paramKey, paramValue) -> {

            if (Objects.nonNull(paramValue)) {
                param.put(paramKey, getRealValue(paramValue));
            }

        });

        IHandler randomParamHandler = new RandomParamHandler();
        IHandler cardHandler = new IdCardHandler();
        IHandler phoneNoHandler = new PhoneNoHandler();
        IHandler emailHandler = new EmailHandler();
        IHandler dateHandler = new DateParamHandler();

        randomParamHandler.setHandler(cardHandler);
        cardHandler.setHandler(phoneNoHandler);
        phoneNoHandler.setHandler(emailHandler);
        emailHandler.setHandler(dateHandler);
        randomParamHandler.handle(param);
    }

    private static Object getRealValue(Object paramValue) {
        // 获取SQL语句对应的真实值
        Object newParamValue = null;

        try {
            if (paramValue instanceof String) {
                newParamValue = getSqlValue(paramValue.toString(), String.class);
            }else {
                // 如果值不满足处理条件，则保留原值
                newParamValue = paramValue;
            }
        } catch (SQLException e) {
            logger.error("数据库连接失败！", e);
        }
        return newParamValue;
    }

    public static String getSqlValue(String value, Type type) throws SQLException {
        // excel 支持sql语句，格式：sql:dataSourceName:select XXX
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

    public static boolean isValueTrue(Object value) {
        // 只有 1 或 Y 返回true
        return value != null && ("1".equalsIgnoreCase(value.toString()) ||
                Constants.EXCEL_YES.equalsIgnoreCase(value.toString()));
    }

    public static boolean isExpectSuccess(Map<String, Object> param) {
        // 是否期望结果为Y
        return isValueTrue(param.get(Constants.ASSERT_RESULT));
    }

    public static boolean isAutoTest(Map<String, Object> param) {
        // 是否需要自动化测试
        return isValueTrue(param.get(Constants.AUTO_TEST));
    }

    public static boolean isHttpModeNoNull(Map<String, Object> param) {
        // 当请求类型不为空是返回true
        return !(param.get(Constants.HTTP_MODE) == null || "".equals(param.get(Constants.HTTP_MODE)));
    }

    public static boolean isHttpHostNoNull(Map<String, Object> param) {
        // 当请求IP地址或域名不为空时返回true
        return !(param.get(Constants.HTTP_HOST) == null || "".equals(param.get(Constants.HTTP_HOST)));
    }

    public static boolean isHttpMethodNoNull(Map<String, Object> param) {
        // 当请求URI路径不为空时返回true
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

    public static boolean isExpectedResultNoNull(Map<String, Object> param) {
        // 判断 excel 中是否有预期断言值
        return !(param.get(Constants.EXPECTED_RESULT) == null || "".equals(param.get(Constants.EXPECTED_RESULT)));
    }

    public static boolean isSidParam(Map<String, Object> param) {
        // excel入参中是否包含sid参数
        return param.get("sid") != null && !"".equals(param.get("sid"));
    }

    public static boolean isActionInfoParam(Map<String, Object> param) {
        // excel入参中是否包含 actionInfo 参数
        return param.get("actionInfo") != null && !"".equals(param.get("actionInfo"));
    }

    public static String getParamName(MethodMeta methodMeta, int index) {
        // 获取参数名称
        if (methodMeta.getParamTypes().length == 1) {
            return methodMeta.getMultiTimeField();
        } else {
            // 多个入参，其中一个入参类似Request<Integer>
            return methodMeta.getParamNames()[index];
        }
    }

    public static String getJSONString(Object obj) {
        // 对象转json
        return getJSONString(obj, true);
    }

    private static String getJSONString(Object obj, boolean prettyFormat) {
        // 对象转json
        return getJSONStringWithDateFormat(obj, prettyFormat, null);
    }

    public static String handleDataList(List<Map<Integer, Object>> datalist, String value, Type type, String sql) {
        // 处理查询出的结果集
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

    public static boolean isExcelValueEmpty(Object value) {
        // 判断excel中的值是否为空
        return value == null || org.apache.commons.lang3.StringUtils.isEmpty(value.toString()) ||
                Constants.EXCEL_NULL.equalsIgnoreCase(value.toString());
    }

    public static String parIsSetName(String fieldName) {
        // 拼接boolean字段方法
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static String parSetName(String fieldName, String fieldType) {
        // 拼接在某属性的 set方法
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        if (("Boolean".equals(fieldType) || "boolean".equals(fieldType)) && fieldName.startsWith("is")) {
            fieldName = fieldName.substring(2);
        }
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static String lowerFirst(String str) {
        // 把首字母转为小写
        if (Objects.isNull(str)||str.length()==0) {
            return str;
        }
        return str.replaceFirst(str.substring(0, 1), str.substring(0, 1).toLowerCase());
    }

    public static boolean isParamTypeExtendsBaseRequest(Method method, int paramIndex) throws Exception {
        // 判断入参是否是继承的 BaseRequest 类
        if (method.getGenericParameterTypes()[paramIndex] instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) (method.getGenericParameterTypes()[paramIndex])).getRawType();
            if (BaseRequest.class.isAssignableFrom((Class<?>) rawType)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Object> getParamContextWithoutExtraInfo(Map<String, Object> param) {
        // 移除excel入参中的关键字字段
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

    public static String getString(Map<?, ?> map, Object key) {
        // 获取Key对应Value，并转换成String
        if (map.get(key) == null || "".equals(map.get(key).toString())) {
            return null;
        } else {
            return map.get(key).toString();
        }
    }

    private static boolean isLanguageParam(Map<String, Object> param) {
        // excel入参中是否包含language参数
        return param.get("language") != null && !"".equals(param.get("language"));
    }

    public static boolean isDependencyIndexNoNull(Map<String, Object> param) {
        // 判断excel测试用例中是否存在dependencyIndex字段
        return param.get(Constants.DEPENDENCY_INDEX) != null && !"".equals(param.get(Constants.DEPENDENCY_INDEX));
    }

    public static String getJSONStringWithDateFormat(Object obj, boolean prettyFormat, String dateFormat) {
        // 对象转json,并格式化输出
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

    public static Map<String, Object> getParameters(Map<String, Object> context) {
        // 获取请求需要的入参
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

    private static List<String> getRemoveKey() {
        // 非Http请求入参字段清单,移除框架关键字
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

    public static void checkKeyWord(Map<String, Object> context) {
        // Http接口入参字段检查
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
