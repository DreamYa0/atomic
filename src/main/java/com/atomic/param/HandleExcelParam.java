package com.atomic.param;

import com.atomic.param.handler.DateParamHandler;
import com.atomic.param.handler.EmailHandler;
import com.atomic.param.handler.IHandler;
import com.atomic.param.handler.IdCardHandler;
import com.atomic.param.handler.PhoneNoHandler;
import com.atomic.param.handler.RandomParamHandler;
import com.atomic.tools.sql.SqlTools;
import com.atomic.util.DataSourceUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


/**
 * @author dreamyao
 * @version 1.1.0 Created by dreamyao on 2017/5/29.
 * @title 通用excel中关键字解析工具类
 */
public final class HandleExcelParam {

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
                // param.put(key, randomParamValue(param.get(key)));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // handleUserKey(key, param);
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
}
