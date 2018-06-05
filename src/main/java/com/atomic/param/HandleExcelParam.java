package com.atomic.param;

import com.atomic.param.handler.DateParamHandler;
import com.atomic.param.handler.EmailHandler;
import com.atomic.param.handler.IHandler;
import com.atomic.param.handler.IdCardHandler;
import com.atomic.param.handler.PhoneNoHandler;
import com.atomic.param.handler.RandomParamHandler;
import com.atomic.tools.sql.SqlTools;
import com.atomic.util.DataSourceUtils;
import com.atomic.util.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.testng.ITestResult;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * @author dreamyao
 * @version 1.1.0 Created by dreamyao on 2017/5/29.
 * @title 通用excel中关键字解析工具类
 */
public final class HandleExcelParam {

    private static final Logger logger = LoggerFactory.getLogger(HandleExcelParam.class);

    private HandleExcelParam() {
    }

    /**
     * 执行测试之前替换excel中的特殊字段为真实的值
     * @param param
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws InvocationTargetException
     */
    public static void getDataBeforeTest(SqlTools sqlTools, Map<String, Object> param) throws IOException, NoSuchMethodException, IllegalAccessException, SQLException, InvocationTargetException {
        // 把入参的 sql 设置为真实的值
        param.keySet().stream().filter(key -> param.get(key) != null).forEach(key -> {
            try {
                param.put(key, getRealValue(sqlTools, param, key));
            } catch (SQLException e) {
                e.printStackTrace();
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

    /**
     * 获取SQL语句对应的真实值
     * @param sqlTools
     * @param param
     * @param field
     * @return
     * @throws SQLException
     */
    private static Object getRealValue(SqlTools sqlTools, Map<String, Object> param, String field) throws SQLException {
        Object value = param.get(field);
        if (value instanceof String) {
            value = getSqlValue(sqlTools, value.toString(), String.class);
            // 把入参里面的 sql 语句改成真实的值
            param.put(field, value);
        }
        return value;
    }

    /**
     * excel 支持sql语句，格式：sql:dataBaseName:select XXX
     * @param value
     * @param type
     * @return
     * @throws SQLException
     */
    private static String getSqlValue(SqlTools sqlTools, String value, Type type) throws SQLException {
        // 格式：sql:dataBaseName:select XXX
        if (value.startsWith("sql:")) {
            value = value.substring(4);
            int index = value.indexOf(":");
            if (index > -1) {
                String dataSourceName = value.substring(0, index);
                String sql = value.substring(index + 1);
                List<Map<Integer, Object>> datalist = DataSourceUtils.queryData(sqlTools, dataSourceName, sql);
                value = StringUtils.handleDataList(datalist, value, type, sql);
            }
        }
        return value;
    }

    /**
     * 把excel入参中多个sheet，组合为真正入参的map集合
     * @param instance 测试类实列
     * @param context  接口入参map集合
     * @return paramter map集合
     */
    public static Map<String, Object> assemblyParamMap2RequestMap(ITestResult testResult, Object instance, Map<String, Object> context) {
        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(context))) {
            Set<String> keys = context.keySet();
            for (String key : keys) {
                Object value = context.get(key);
                if (Objects.isNull(value)|| "".equals(value)) {
                    ExcelUtils excel = new ExcelUtils();
                    try {
                        List<Map<String, Object>> maps = excel.readDataByRow(testResult, instance, key);
                        Map<String, Object> map = maps.get(Integer.valueOf(context.get(Constants.CASE_INDEX).toString()) - 1);
                        Map<String, Object> assemblyMap = assemblyParamMap2RequestMap(testResult, instance, map);
                        assemblyMap.remove(Constants.CASE_INDEX);
                        context.put(key, assemblyMap);
                    } catch (Exception e) {
                        // 如果Sheet不存在，则按照原逻辑处理
                    }
                }
            }
        }
        return context;
    }
}
