package com.atomic.tools.rollback;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.DbUtil;
import cn.hutool.db.sql.SqlFormatter;
import com.atomic.tools.rollback.db.Change;
import com.atomic.tools.rollback.db.ChangeType;
import com.atomic.tools.rollback.db.Changes;
import com.atomic.tools.rollback.db.Table;
import com.atomic.tools.rollback.db.Value;
import com.atomic.util.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;


/**
 * 数据回滚工具类
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/27.
 */
public final class RollBackManager {

    private static final Logger logger = LoggerFactory.getLogger(RollBackManager.class);
    private static final RollBackManager INSTANCE = new RollBackManager();

    private RollBackManager() {

    }

    public static RollBackManager newInstance() {
        return INSTANCE;
    }

    void setStartPoint(String dbName, Changes changes, String... tableNames) {
        // 开启监听
        createStartPoint(dbName, changes, tableNames);
    }

    void setEndPoint(String dbName, Changes changes) {
        // 关闭监听并进行数据回滚
        changes.setEndPointNow();
        runRollback(dbName, changes);
    }

    private void createStartPoint(String dbName, Changes changes, String... tableNames) {
        Table[] tables = new Table[tableNames.length];
        DataSource dataSource = DataSourceUtils.getDataSource(dbName);
        for (int i = 0; i < tableNames.length; i++) {
            Table t = new Table(dataSource, tableNames[i]);
            tables[i] = t;
        }
        changes.setTables(tables);
        changes.setStartPointNow();
    }


    private void runRollback(String dbName, Changes changes) {
        List<Change> changeList = changes.getChangesList();
        System.out.println("------------------- rollback sql" + " -------------------");
        for (Change change : changeList) {
            String sql = editSql(change);
            try {
                DbUtil.use(DataSourceUtils.getDataSource(dbName)).execute(sql);
                System.out.println(StrUtil.format("\nSQL -> {}", SqlFormatter.format(sql)));
            } catch (SQLException e) {
                /*Reporter.log("执行数据回滚SQL语句异常! sql：" + sql ,true);
                e.printStackTrace();*/
                continue;
            }
        }
        System.out.println("------------------- rollback sql" + " -------------------");
    }

    private String editSql(Change change) {
        ChangeType type = change.getChangeType();
        String tableName = change.getDataName();
        if ("CREATION".equals(type.name())) {
            List<Value> values = change.getRowAtEndPoint().getValuesList();
            String id = values.get(0).getColumnName();
            Object value = values.get(0).getValue();
            if (value instanceof String) {
                return "delete from " + tableName + " where " + id + " = " + "'" + value + "'" + "";
            }
            return "delete from " + tableName + " where " + id + " = " + value + "";
        } else if ("DELETION".equals(type.name())) {
            StringBuilder sql = new StringBuilder("insert into " + tableName + " values(");
            List<Value> valuesList = change.getRowAtStartPoint().getValuesList();
            for (Value value : valuesList) {
                Object columenValue = value.getValue();
                if (columenValue == null) {
                    sql.append((Object) null).append(",");
                } else {
                    sql.append("'").append(columenValue).append("',");
                }
            }
            sql = new StringBuilder(sql.substring(0, sql.length() - 1));
            sql.append(")");
            return sql.toString();
        } else if ("MODIFICATION".equals(type.name())) {
            StringBuilder sql = new StringBuilder("update " + tableName + " SET ");
            List<Value> valuesList = change.getRowAtStartPoint().getValuesList();
            for (Value value : valuesList) {
                Object columenValue = value.getValue();
                String columnName = value.getColumnName();
                if (columenValue == null) {
                    sql.append(columnName).append("=").append((Object) null).append(",");
                } else {
                    sql.append(columnName).append("='").append(columenValue).append("' ,");
                }
            }
            sql = new StringBuilder(sql.substring(0, sql.length() - 1));
            sql.append(" where ")
                    .append(valuesList.get(0).getColumnName())
                    .append(" = ")
                    .append(valuesList.get(0).getValue());
            return sql.toString();
        }
        return "";
    }
}
