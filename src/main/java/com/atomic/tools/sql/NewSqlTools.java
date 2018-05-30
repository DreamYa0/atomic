package com.atomic.tools.sql;

import com.atomic.config.CenterConfig;
import com.atomic.config.GlobalConfig;
import com.atomic.exception.ParameterException;
import com.atomic.exception.QueryDataException;
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

import static com.atomic.param.Constants.DRIVER_CLASS_NAME;
import static com.atomic.param.Constants.JDBC_IP;
import static com.atomic.param.Constants.JDBC_PASSWORD;
import static com.atomic.param.Constants.JDBC_USER;
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
public class NewSqlTools {

    private static final NewSqlTools INSTANCE = new NewSqlTools();
    private final String dbPassword;
    private final String dbUser;
    private final List<Request> requestList = Lists.newArrayList();
    private final List<Map<String, Object>> mapList = Lists.newArrayList();
    private final List<String> pkValues = Lists.newCopyOnWriteArrayList();
    private volatile String ip;
    private volatile String profile;
    private volatile Connection connection;

    private NewSqlTools() {
        //加载环境配置文件
        GlobalConfig.load();
        profile = GlobalConfig.getProfile();
        Map<String, String> maps = CenterConfig.newInstance().readPropertyConfig(profile);
        ip = maps.get(JDBC_IP);
        dbUser = maps.get(JDBC_USER);
        dbPassword = maps.get(JDBC_PASSWORD);
    }

    private NewSqlTools(String ip, String dbUser, String dbPassword) {
        this.ip = ip;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public static NewSqlTools newInstance(String ip, String dbUser, String dbPassword) {
        return new NewSqlTools(ip, dbUser, dbPassword);
    }

    public static NewSqlTools newInstance() {
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

    public NewSqlTools connect(String database) {
        if (database == null || "".equals(database)) {
            throw new ParameterException("数据库名不能为空！");
        }
        try {
            if (connection != null) {
                connection.close();
            }
            Class.forName(DRIVER_CLASS_NAME).newInstance();
            String connectString = "jdbc:mysql://" + ip + "/" + database + "?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=round&useSSL=false";
            connection = DriverManager.getConnection(connectString, dbUser, dbPassword);

            /*CoreConnectionPool connectionPool = new CoreConnectionPool();
            connectionPool.setDriver(DRIVER_CLASS_NAME);
            connectionPool.setUrl("jdbc:mysql://" + ip + "/" + database + "?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=round&useSSL=false");
            connectionPool.setUser(dbUser);
            connectionPool.setPassword(dbPassword);
            connectionPool.setMinConnections(5);
            connectionPool.setMaxConnections(10);
            connectionPool.init();
            connection = connectionPool.getConnection();*/

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
    public NewSqlTools execute(String sql, Object... params) {
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
    public NewSqlTools query(String sql, Object... params) {
        try {
            synchronized (this) {
                querySql(connection, mapList, sql, params);
            }
        } catch (SQLException e) {
            Reporter.log("[NewSqlTools#query()]:{} ---> 数据查询异常！", true);
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 获取查询结果的封装对象
     * @param querySql   Sql语句
     * @param parameters Sql语句参数
     * @return 获取查询结果的封装对象
     */
    public NewSqlTools request(String querySql, Object... parameters) {
        if (connection == null) {
            throw new QueryDataException("没有建立数据库连接不能进行数据库操作！");
        }
        try {
            String url = connection.getMetaData().getURL();
            String username = connection.getMetaData().getUserName();
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
     * 获取结果
     * @return 查询Sql产生的结果
     */
    public <T> List<T> collect(T t) {
        disconnect();
        synchronized (this) {
            return handle2List(t, mapList, requestList, pkValues);
        }
    }

    /**
     * 关闭数据库连接
     */
    public void disconnect() {
        SqlUtils.disconnect(connection);
    }
}
