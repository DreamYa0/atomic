package com.atomic.param;

import com.atomic.param.handler.DateParamHandler;
import com.atomic.param.handler.EmailHandler;
import com.atomic.param.handler.IHandler;
import com.atomic.param.handler.IdCardHandler;
import com.atomic.param.handler.PhoneNoHandler;
import com.atomic.param.handler.RandomParamHandler;
import com.atomic.tools.sql.SqlTools;
import com.atomic.util.DataSourceUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

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

    /*public static void handleUserKey(String key, Map<String, Object> param) {
        if ("userkey".equalsIgnoreCase(key)) {
            String value = param.get(key).toString();
            String[] values = value.split(":");
            if (values.length == 2) {
                UserKeyConsole userKeyConsole = new UserKeyConsole();
                if ("no".equalsIgnoreCase(values[1])) {
                    param.put(key, userKeyConsole.getUserKey(values[0], false));
                } else if ("yes".equalsIgnoreCase(values[1])) {
                    param.put(key, userKeyConsole.getUserKey(values[0], true));
                }
            }
        }
    }*/

    /**
     * 用随机数来替换值
     * @param value
     */
    public static Object randomParamValue(Object value) {
        String[] values = value.toString().split(":");
        if (values.length > 0) {
            if (Constants.EXCEL_RANDOM.equals(values[0])) {
                if ("String".equals(values[1])) {
                    return RandomStringUtils.randomAlphabetic(Integer.valueOf(values[2]));
                } else if ("int".equals(values[1])) {
                    return randomInt(values[2], values[3]);
                } else if ("double".equals(values[1])) {
                    return randomDouble(values[2], values[3]);
                } else if ("float".equals(values[1])) {
                    return randomFloat(values[2], values[3]);
                } else if ("long".equals(values[1])) {
                    return randomLond(values[2], values[3]);
                }
            }
        }
        return value;
    }

    private static int randomInt(String startInclusive, String endInclusive) {
        return RandomUtils.nextInt(Integer.valueOf(startInclusive), Integer.valueOf(endInclusive));
    }

    private static double randomDouble(String startInclusive, String endInclusive) {
        return RandomUtils.nextDouble(Double.valueOf(startInclusive), Double.valueOf(endInclusive));
    }

    private static long randomLond(String startInclusive, String endInclusive) {
        return RandomUtils.nextLong(Long.valueOf(startInclusive), Long.valueOf(endInclusive));
    }

    private static float randomFloat(String startInclusive, String endInclusive) {
        return RandomUtils.nextFloat(Float.valueOf(startInclusive), Float.valueOf(endInclusive));
    }
}
