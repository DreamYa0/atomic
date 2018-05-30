package com.atomic.util;

import com.atomic.exception.GetBeanException;
import com.atomic.tools.sql.SqlTools;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
public final class DataSourceUtils {

    private static ThreadLocal<DataSource> dataSourceThreadLocal = new ThreadLocal<>();

    private DataSourceUtils() {
    }

    /**
     * 根据dataSource名称查询数据
     * @param dataSourceName 数据源名称
     * @param sql            SQL语句
     * @return 结果集
     * @throws SQLException
     */
    public static List<Map<Integer, Object>> queryData(String dataSourceName, String sql) throws SQLException {
        Connection conn = getConnection(dataSourceName);
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        // select value1,value2
        List<Map<Integer, Object>> list = Lists.newArrayList();
        handleResultSet(resultSet, list);
        resultSet.close();
        ps.close();
        conn.close();
        return list;
    }

    /**
     * 根据数据库名称查询数据
     * @param sqlTools     数据库操作工具
     * @param dataBaseName 数据库名称
     * @param sql          SQL语句
     * @return 结果集
     * @throws SQLException
     */
    public static List<Map<Integer, Object>> queryData(SqlTools sqlTools, String dataBaseName, String sql) throws SQLException {
        sqlTools.connect(dataBaseName);
        ResultSet resultSet = sqlTools.getResult(sql);
        // select value1,value2
        List<Map<Integer, Object>> list = Lists.newArrayList();
        handleResultSet(resultSet, list);
        resultSet.close();
        // 关闭后会导致给测试用例的sqlTools失效
        // sqlTools.disconnect();
        return list;
    }

    /**
     * 处理ResultSet结果集
     * @param resultSet 结果集
     * @param list      List集合
     * @throws SQLException
     */
    private static void handleResultSet(ResultSet resultSet, List<Map<Integer, Object>> list) throws SQLException {
        int size = resultSet.getMetaData().getColumnCount();
        while (resultSet.next()) {
            Map<Integer, Object> map = Maps.newHashMap();
            for (int i = 0; i < size; i++) {
                // 是从1开始
                map.put(i, resultSet.getObject(i + 1));
            }
            list.add(map);
        }
    }

    private static Connection getConnection(String dataSourceName) throws SQLException {
        DataSource dataSource = getDataSource();
        return dataSource.getConnection();
    }

    /**
     * 获取数据源
     * @return 获取dataSource
     */
    public static DataSource getDataSource() {
        DataSource dataSource = null;
        try {
            if (dataSourceThreadLocal.get() == null) {
                dataSource = (DataSource) ApplicationUtils.getBean(DataSource.class);
                dataSourceThreadLocal.set(dataSource);
            } else {
                dataSource = dataSourceThreadLocal.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dataSource == null) {
            throw new GetBeanException("获取dataSource失败！");
        }
        return dataSource;
    }

}
