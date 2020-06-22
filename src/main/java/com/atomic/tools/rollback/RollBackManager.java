package com.atomic.tools.rollback;

import cn.hutool.db.DbUtil;
import com.atomic.annotations.AnnotationUtils;
import com.atomic.exception.AnnotationException;
import com.atomic.tools.db.Change;
import com.atomic.tools.db.ChangeType;
import com.atomic.tools.db.Changes;
import com.atomic.tools.db.Table;
import com.atomic.tools.db.Value;
import com.atomic.util.DataSourceUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;


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

    /**
     * 开启监听
     *
     * @param changes    Changes对象
     * @param tableNames 数据库表名
     */
    void setStartPoint(String dbName, Changes changes, String... tableNames) {
        createStartPoint(dbName, changes, tableNames);
    }

    /**
     * 关闭监听并进行数据回滚
     *
     * @param changes Changes对象
     */
    void setEndPoint(String dbName, Changes changes) {
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
        for (Change change : changeList) {
            String sql = editSql(change);
            try {
                DbUtil.use(DataSourceUtils.getDataSource(dbName)).execute(sql);
            } catch (SQLException e) {
                /*Reporter.log("执行数据回滚SQL语句异常! sql：" + sql ,true);
                e.printStackTrace();*/
                continue;
            }
            System.out.println("----------------回滚SQL: " + sql + "------------------");
        }
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

    /**
     * 获取监控的数据库表名
     * @param testNGMethods 测试方法
     * @param changesMap    数据库名称集合
     * @return 监控的数据库表名
     */
    Map<String, String[]> getTableNames4Scenario(ITestNGMethod[] testNGMethods, Map<String, Changes> changesMap) {
        Map<String, String[]> tableNameList = Maps.newHashMap();
        for (ITestNGMethod testNGMethod : testNGMethods) {
            Method method = testNGMethod.getConstructorOrMethod().getMethod();
            if (AnnotationUtils.isRollBackMethod(method) && AnnotationUtils.isScenario(method)) {
                String dbName = AnnotationUtils.getDbName(testNGMethod.getConstructorOrMethod().getMethod());
                changesMap.put(dbName, new Changes());

                String[] tableNames = AnnotationUtils.getTableName(testNGMethod.getConstructorOrMethod().getMethod());
                tableNameList.put(dbName, tableNames);
            } else if (AnnotationUtils.isRollBackAllMethod(method) && AnnotationUtils.isScenario(method)) {
                // 实现多数据库数据回滚
                try {
                    Multimap<String, String> multimap = AnnotationUtils.getDbNameAndTableName(
                            testNGMethod.getConstructorOrMethod().getMethod());
                    Set<String> set = multimap.keySet();
                    for (String dbName : set) {
                        changesMap.put(dbName, new Changes());
                    }
                    set.forEach(dbName -> {
                        List<String> list = (List<String>) multimap.get(dbName);
                        tableNameList.put(dbName, (String[]) list.toArray());
                    });
                } catch (AnnotationException e) {
                    e.printStackTrace();
                }
            }
        }
        return tableNameList;
    }
}
