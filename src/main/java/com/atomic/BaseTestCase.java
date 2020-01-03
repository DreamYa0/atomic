package com.atomic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jodd.db.DbQuery;
import jodd.db.oom.DbOomQuery;
import org.assertj.db.type.Request;
import org.assertj.db.type.Row;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static com.atomic.util.DataSourceUtils.getDataSource;

/**
 * @author dreamyao
 * @version 1.1.0
 */
public abstract class BaseTestCase<T> extends CommonTest<T> {

    protected final void insert(String insertSql) {
        String db_tab;
        String PrimaryKey;
        String PrimaryValue;
        db_tab = insertSql.substring(insertSql.indexOf("INSERT INTO") + 12, insertSql.indexOf("(") - 1);
        PrimaryKey = insertSql.substring(insertSql.indexOf("(") + 1, insertSql.indexOf(","));
        PrimaryValue = insertSql.substring(insertSql.indexOf("VALUES (") + 8, insertSql.indexOf(",", insertSql.indexOf("VALUES (") + 8));
        String delSQL = "delete from " + db_tab + " where " + PrimaryKey + " = " + PrimaryValue;
        DataSource dataSource = getDataSource();
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.execute(delSQL);
            stmt.execute(insertSql);
            System.out.println("---------------------------SQL: " + insertSql + "---------------------------");
            System.out.println("---------------------------SQL: " + delSQL + "---------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化数据并返回初始化数据的主键ID
     * @param insertSql SQL语句
     * @param pkName    主键名称
     */
    protected final long insert(String insertSql, String... pkName) {
        String dbTableName = insertSql.substring(insertSql.indexOf("INSERT INTO") + 12, insertSql.indexOf("(") - 1);
        String primaryKey = insertSql.substring(insertSql.indexOf("(") + 1, insertSql.indexOf(","));
        String primaryValue = insertSql.substring(insertSql.indexOf("VALUES (") + 8, insertSql.indexOf(",", insertSql.indexOf("VALUES (") + 8));
        String delSQL = "delete from " + dbTableName + " where " + primaryKey + " = " + primaryValue;
        DataSource dataSource = getDataSource();
        try (Connection connection = dataSource.getConnection()) {
            DbQuery dbQuery = new DbQuery(connection, delSQL);
            dbQuery.executeUpdate();
            dbQuery.close();
            DbOomQuery dbOomQuery = new DbOomQuery(connection, insertSql);
            dbOomQuery.setGeneratedColumns(pkName);
            dbOomQuery.executeUpdate();
            long key = dbOomQuery.getGeneratedKey();
            dbOomQuery.close();
            System.out.println("---------------------------SQL: " + insertSql + "---------------------------");
            System.out.println("---------------------------SQL: " + delSQL + "---------------------------");
            return key;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 数据库请求结果的封装，用于数据库端口
     * <p>
     * 例如：assertThat(getRequest(sql)).Row().value()
     * </p>
     * @param querySql 查询Sql语句
     * @return 结果的封装
     */
    protected final Request getRequest(String querySql, Object... parameters) {
        DataSource dataSource = getDataSource();
        return new Request(dataSource, querySql, parameters);
    }

    /**
     * Sql 语句获取数据
     * @param querySql 查询Sql语句
     * @return 结果集
     */
    protected final List<Map<String, Object>> query(String querySql) {
        DataSource dataSource = getDataSource();
        return query(dataSource, querySql);
    }

    private List<Map<String, Object>> query(DataSource dataSource, String sql) {
        Request request = new Request(dataSource, sql);
        return query(request);
    }

    private List<Map<String, Object>> query(Request request) {
        List<Row> rows = request.getRowsList();
        List<Map<String, Object>> datas = Lists.newArrayList();
        Map<String, Object> map = Maps.newHashMap();
        for (Row row : rows) {
            row.getValuesList().forEach(value -> map.put(value.getColumnName(), value.getValue()));
            datas.add(map);
            map.clear();
        }
        return datas;
    }
}
