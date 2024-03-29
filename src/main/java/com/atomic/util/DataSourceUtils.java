package com.atomic.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.ds.simple.SimpleDataSource;
import com.atomic.config.ConfigConstants;
import com.atomic.config.AtomicConfig;
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

    private static final ConcurrentMap<String, DataSource> DATA_SOURCE_CONCURRENT_MAP =
            new ConcurrentHashMap<>(16);

    private DataSourceUtils() {

    }

    public static List<Map<Integer, Object>> queryData(String dbName, String sql) throws SQLException {
        // 根据数据库名称查询数据
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

    private static void handleResultSet(ResultSet resultSet, List<Map<Integer, Object>> list) throws SQLException {
        // 处理ResultSet结果集
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

    public static DataSource getDataSource(String dbName) {
        // 获取数据源
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
            final String dbUrl = StrUtil.format(
                    "jdbc:mysql://{}/{}" +
                    "?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=round&useSSL=false",
                    AtomicConfig.getStr(ConfigConstants.DATABASE_URL),
                    dbName);
            DataSource dataSource = new SimpleDataSource(
                    dbUrl, AtomicConfig.getStr(ConfigConstants.DATABASE_USER_NAME),
                    AtomicConfig.getStr(ConfigConstants.DATABASE_PASSWORD));
            DATA_SOURCE_CONCURRENT_MAP.put(dbName, dataSource);
            return dataSource;
        }
    }
}