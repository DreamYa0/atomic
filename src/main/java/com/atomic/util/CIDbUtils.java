package com.atomic.util;

import com.atomic.param.entity.AutoTestAssert;
import com.atomic.param.entity.AutoTestProject;
import com.atomic.param.entity.AutoTestResult;
import com.atomic.param.entity.QaScenarioData;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.testng.Reporter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by DreamYao on 2016/12/17.
 * 数据库连接工具类，用作对数据库进行查询操作，目前只提供查询操作，
 * 后期根据需要再提供更新、插入、删除操作
 * 此类只供持续集成项目调用，接口测试项目不得调用
 */
public final class CIDbUtils {

    private static final String URL = "jdbc:mysql://10.199.5.130:3306/atomic_autotest?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=round&useSSL=false";
    private static final String USER = "autotest";
    private static final String PASSWORD = "123456";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static Connection conn;

    private CIDbUtils() {
    }

    /**
     * 数据库连接方法
     * @return
     */
    private static Connection getConnection() {
        Connection conn = null;
        DbUtils.loadDriver(DRIVER);//加载数据库驱动如果成功返回true否则返回false
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);//连接数据库
        } catch (SQLException e) {
            Reporter.log("连接数据库异常！", true);
        }
        return conn;
    }

    /**
     * 返回数据库响应查询语句结果的Json数据封装结果值
     * 这个类负责在数据库中进行数据的查询，和对查询结果进行数据封装
     */
    public static AutoTestResult queryQaMethodValue(String sqlString, Object[] params) {
        conn = getConnection();
        /**
         * QueryRunner：简化了数据库的增删改查操作，用来替代JDBC中的executeQuery、executeUpdate等方法，
         * 通过与ResultSetHandler配合使用，能够大大减少所写的代码
         * 1.update：执行INSERT、UPDATE或者DELETE等SQL语句
         * 2.insert：执行INSERT语句
         * 3.query：执行SELECT语句
         * 另外，可以调用相关的batch方法来批量操作INSERT、UPDATE或者DELETE等SQL语句
         */
        QueryRunner qr = new QueryRunner();//创建SQL执行类对象
        AutoTestResult obj = null;
        try {
            /**
             * 执行SQL查询语句
             * org.apache.commons.dbutils.handlers
             * 该包中的类都是对于前述ResultSetHandler的实现
             * 1.ArrayHandler：将ResultSet中的第一行的数据转化成对象数组
             * 2.ArrayListHandler：将ResultSet中所以的数据转化成List，List中存放的是Object[]
             * 3.BeanHandler：将ResultSet中第一行的数据转化成类对象
             * 4.BeanListHandler：将ResultSet中所有数据转化成List，List中存放的是类对象
             * 5.ColumnListHandler：将ResultSet中某一列的数据存成List，List中存放的是Object对象
             * 6.KeyedHandler：将ResultSet中的每一行数据都封装到一个Map里，再把这些map再存到一个map里，其key为指定的key
             * 7.MapHandler：将ResultSet中第一行的数据存成Map映射
             * 8.MapListHandler：将ResultSet中所有的数据存成List，List中存放的是Map
             * 8.ScalarHandler：将ResultSet中一条记录的某一列数据存成Object
             */
            obj = qr.query(conn, sqlString, new BeanHandler<AutoTestResult>(AutoTestResult.class), params);
        } catch (SQLException e) {
            Reporter.log("[CIDbUtils#queryQaMethodValue]异常信息：{}", true);
        } finally {
            DbUtils.closeQuietly(conn);
        }
        return obj;
    }

    /**
     * 返回数据库响应查询语句结果的Json数据封装结果值
     * 这个类负责在数据库中进行数据的查询，和对查询结果进行数据封装
     */
    public static AutoTestProject queryQaProjectValue(String sqlString, Object[] params) {
        conn = getConnection();
        QueryRunner qr = new QueryRunner();//创建SQL执行类对象
        AutoTestProject obj = null;
        try {
            obj = qr.query(conn, sqlString, new BeanHandler<>(AutoTestProject.class), params);
        } catch (SQLException e) {
            Reporter.log("[CIDbUtils#queryQaProjectValue]异常信息：{}", true);
        } finally {
            DbUtils.closeQuietly(conn);
        }
        return obj;
    }

    /**
     * 查询智能化断言内容
     * @param sqlString
     * @param params
     * @return
     */
    public static AutoTestAssert queryQaAutoAssetValue(String sqlString, Object[] params) {
        conn = getConnection();
        QueryRunner qr = new QueryRunner();//创建SQL执行类对象
        AutoTestAssert obj = null;
        try {
            obj = qr.query(conn, sqlString, new BeanHandler<>(AutoTestAssert.class), params);
        } catch (SQLException e) {
            Reporter.log("[CIDbUtils#queryQaAutoAssetValue]异常信息：{}", true);
        } finally {
            DbUtils.closeQuietly(conn);
        }
        return obj;
    }

    /**
     * 查询场景测试时保存的测试方法的数据
     * @param sql
     * @param params
     * @return
     */
    public static QaScenarioData queryQaScenarioTestValue(String sql, Object[] params) {
        conn = getConnection();
        QueryRunner qr = new QueryRunner();//创建SQL执行类对象
        QaScenarioData obj = null;
        try {
            obj = qr.query(conn, sql, new BeanHandler<>(QaScenarioData.class), params);
        } catch (SQLException e) {
            Reporter.log("[CIDbUtils#queryQaScenarioTestValue]异常信息：{}", true);
        } finally {
            DbUtils.closeQuietly(conn);
        }
        return obj;
    }

    /**
     * 测试数据入库调用方法
     * @param sqlString
     * @param params
     */
    public static void updateValue(String sqlString, Object[] params) {
        conn = getConnection();
        QueryRunner qr = new QueryRunner();//创建SQL执行类对象
        try {
            qr.update(conn, sqlString, params);
        } catch (SQLException e) {
            Reporter.log("[CIDbUtils#updateValue]异常信息：{}", true);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }
}
