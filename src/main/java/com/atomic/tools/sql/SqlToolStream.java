package com.atomic.tools.sql;

import com.atomic.config.CenterConfig;
import com.atomic.config.GlobalConfig;
import com.atomic.exception.ParameterException;
import com.atomic.exception.QueryDataException;
import com.atomic.param.Constants;
import com.google.common.collect.Lists;
import org.assertj.db.type.Request;
import org.assertj.db.type.Source;
import org.testng.Reporter;

import javax.annotation.concurrent.ThreadSafe;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atomic.tools.sql.SqlUtils.excuteSql;
import static com.atomic.tools.sql.SqlUtils.handle2List;
import static com.atomic.tools.sql.SqlUtils.querySql;


/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2018/05/30 10:48
 */
@ThreadSafe
public class SqlToolStream {

    private static final SqlToolStream INSTANCE = new SqlToolStream();
    private final List<Map<String, Object>> mapList = Lists.newArrayList();
    private final List<Request> requestList = Lists.newArrayList();
    private final List<String> pkValues = Lists.newArrayList();
    private volatile String dbPassword = "";
    private volatile String dbUser = "";
    private volatile String dbIp = "";
    private volatile Connection connection;

    private SqlToolStream() {
        GlobalConfig.load();//加载环境配置文件
        String profile = GlobalConfig.getProfile();
        Map<String, String> maps = CenterConfig.newInstance().readPropertyConfig(profile);
        dbIp = maps.get(Constants.JDBC_IP);
        dbUser = maps.get(Constants.JDBC_USER);
        dbPassword = maps.get(Constants.JDBC_PASSWORD);
    }

    public static SqlToolStream sqlToolFactory() {
        return INSTANCE;
    }

    public static Map<String, Object> toMap() {
        return new HashMap<>();
    }

    public static Request toRequest() {
        return new Request();
    }

    public static String toList() {
        return "";
    }

    /**
     * 连接数据库
     * @param databaseName database
     */
    public SqlToolStream connect(String databaseName) {
        if (databaseName == null || "".equals(databaseName)) {
            throw new ParameterException("数据库名不能为空！");
        }
        try {
            if (connection != null) {
                connection.close();
            }
            connection = DriverManager.getConnection(getUrl(databaseName), dbUser, dbPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 执行 U（更新）I（插入）D（删除）操作
     * @param sql Sql语句
     * @return 当前类的引用
     */
    public SqlToolStream execute(String sql, Object... params) {
        if (connection == null) {
            throw new QueryDataException("没有建立数据库连接不能进行数据库操作！");
        }
        excuteSql(connection, pkValues, sql, params);
        return this;
    }

    /**
     * 执行 S（查询）操作
     * @param sql 查询Sql
     * @return 当前类的引用
     */
    public synchronized SqlToolStream query(String sql, Object... params) {
        try {
            querySql(connection, mapList, sql, params);
        } catch (SQLException e) {
            Reporter.log("[SqlToolStream#query()]:{} ---> 数据查询异常！", true);
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 获取结果
     * @return 查询Sql产生的结果
     */
    @Deprecated
    public synchronized List<Map<String, Object>> toResult() {
        disconnect();
        return mapList;
    }

    /**
     * 获取查询结果的封装对象
     * @param querySql   Sql语句
     * @param parameters Sql语句参数
     * @return 获取查询结果的封装对象
     */
    public SqlToolStream request(String querySql, Object... parameters) {
        if (connection == null) {
            throw new QueryDataException("没有建立数据库连接不能进行数据库操作！");
        }
        try {
            String url = connection.getMetaData().getURL();
            Source source = new Source(url, dbUser, dbPassword);
            synchronized (requestList) {
                requestList.add(new Request(source, querySql, parameters));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 收集结果
     * @return 查询Sql产生的结果
     */
    public synchronized <T> List<T> collect(T t) {
        disconnect();
        return handle2List(t, mapList, requestList, pkValues);
    }

    /**
     * 关闭数据库连接
     */
    public void disconnect() {
        SqlUtils.disconnect(connection);
    }

    private String getIpByUrl(String url) {
        Pattern pattern = Pattern.compile("//(.*?)/");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public String getUrl(String databaseName) {
        return "jdbc:mysql://" + dbIp + "/" + databaseName + "?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=round&useSSL=false";
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbUser() {
        return dbUser;
    }
}
