package com.atomic.util;

import cn.hutool.db.ds.DSFactory;
import cn.hutool.setting.Setting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
public final class DataSourceUtils {

    private static ThreadLocal<DataSource> dataSourceThreadLocal = new ThreadLocal<>();
    private static final ConcurrentMap<String, DataSource> DATA_SOURCE_CONCURRENT_MAP =
            new ConcurrentHashMap<>(16);

    private DataSourceUtils() {

    }

    /**
     * 根据数据库名称查询数据
     *
     * @param sql SQL语句
     * @return 结果集
     * @throws SQLException
     */
    public static List<Map<Integer, Object>> queryData(String dbName, String sql) throws SQLException {
        ResultSet resultSet = getResult(dbName, sql);
        // select value1,value2
        List<Map<Integer, Object>> list = Lists.newArrayList();
        handleResultSet(resultSet, list);
        resultSet.close();
        return list;
    }

    private static ResultSet getResult(String dbName, String sqlString) {
        Statement stmt;
        ResultSet rs = null;
        try (Connection connection = DataSourceUtils.getDataSource(dbName).getConnection()) {
            stmt = connection.createStatement();
            stmt.execute(sqlString);
            rs = stmt.getResultSet();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
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

    /**
     * 获取数据源
     *
     * @return 获取dataSource
     */
    public static DataSource getDataSource(String dbName) {
        try {
            DataSource source = DATA_SOURCE_CONCURRENT_MAP.get(dbName);
            if (Objects.nonNull(source)) {
                return source;
            }

            DataSource dataSource = (DataSource) ApplicationUtils.getBean(DataSource.class);
            DATA_SOURCE_CONCURRENT_MAP.put(dbName, dataSource);
            return dataSource;

        } catch (Exception e) {
            // 如果从 spring 容器中获取DataSource 失败，则从加载数据库连接配置文件初始化
            //自定义数据库Setting
            Setting setting = new Setting("db.setting");
            //获取指定配置，第二个参数为分组，用于多数据源，无分组情况下传null
            DSFactory factory = DSFactory.create(setting);
            DataSource dataSource = factory.getDataSource(dbName);
            DATA_SOURCE_CONCURRENT_MAP.put(dbName, dataSource);
            return dataSource;
        }
    }
}
