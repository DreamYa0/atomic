package com.atomic;

import cn.hutool.db.DbUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.assertj.db.type.Request;
import org.assertj.db.type.Row;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.atomic.util.DataSourceUtils.getDataSource;

/**
 * @author dreamyao
 * @version 1.1.0
 */
public abstract class BaseTestCase<T> extends CommonTest<T> {

    protected final void insert(String dbName, String insertSql) {
        String db_tab;
        String PrimaryKey;
        String PrimaryValue;
        db_tab = insertSql.substring(insertSql.indexOf("INSERT INTO") + 12, insertSql.indexOf("(") - 1);
        PrimaryKey = insertSql.substring(insertSql.indexOf("(") + 1, insertSql.indexOf(","));
        PrimaryValue = insertSql.substring(insertSql.indexOf("VALUES (") + 8,
                insertSql.indexOf(",", insertSql.indexOf("VALUES (") + 8));
        String delSQL = "delete from " + db_tab + " where " + PrimaryKey + " = " + PrimaryValue;
        DataSource dataSource = getDataSource(dbName);
        try {
            DbUtil.use(dataSource).execute(delSQL);
            DbUtil.use(dataSource).execute(insertSql);
            System.out.println("---------------------------SQL: " + insertSql + "---------------------------");
            System.out.println("---------------------------SQL: " + delSQL + "---------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected final long insert(String dbName, String insertSql, String... pkName) {
        // 初始化数据并返回初始化数据的主键ID
        String dbTableName = insertSql.substring(insertSql.indexOf("INSERT INTO") + 12, insertSql.indexOf("(") - 1);
        String primaryKey = insertSql.substring(insertSql.indexOf("(") + 1, insertSql.indexOf(","));
        String primaryValue = insertSql.substring(insertSql.indexOf("VALUES (") + 8, insertSql.indexOf(",",
                insertSql.indexOf("VALUES (") + 8));
        String delSQL = "delete from " + dbTableName + " where " + primaryKey + " = " + primaryValue;
        DataSource dataSource = getDataSource(dbName);
        try {
            DbUtil.use(dataSource).execute(delSQL);
            Long key = DbUtil.use(dataSource).executeForGeneratedKey(insertSql);
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
     *
     * @param querySql 查询Sql语句
     * @return 结果的封装
     */
    protected final Request getRequest(String dbName, String querySql, Object... parameters) {
        DataSource dataSource = getDataSource(dbName);
        return new Request(dataSource, querySql, parameters);
    }

    protected final List<Map<String, Object>> query(String dbName, String querySql) {
        // sql 语句获取数据
        DataSource dataSource = getDataSource(dbName);
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
