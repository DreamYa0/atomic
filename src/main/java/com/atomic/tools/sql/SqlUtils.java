package com.atomic.tools.sql;

import com.atomic.exception.QueryDataException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.assertj.db.type.Request;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;
import java.util.Map;

import static com.atomic.param.Constants.DRIVER_CLASS_NAME;


@ThreadSafe
class SqlUtils {

    /**
     * 线程封闭
     */
    private final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public SqlUtils() {
    }

    public SqlUtils(Connection connection) {
        connectionThreadLocal.set(connection);
    }

    /**
     * 生成添加语句
     * @param t:表对应对象
     * @return 添加SQL语句
     */
    public static <T> String createInsertSql(Class<T> t, List<String> list) {
        StringBuilder bf = new StringBuilder();
        bf.append("insert into ");
        bf.append(t.getSimpleName());
        bf.append("(");
        for (String str : list) {
            bf.append(str);
            bf.append(",");
        }
        bf.delete(bf.length() - 1, bf.length());
        bf.append(") values(");
        for (int i = 0; i < list.size(); i++) {
            bf.append("?,");
        }
        bf.delete(bf.length() - 1, bf.length());
        bf.append(")");
        return bf.toString();
    }

    /**
     * 单表查询  无条件(传入空的List<String>)
     * @param t:表对应的的对象 slist:查询条件参数表达式集合(例:sno>? ; sno=? ; sno in(?,?))
     * @return 查询SQL语句
     */
    public static <T> String createSelectSql(Class<T> t, List<String> slist) {
        StringBuilder bf = new StringBuilder();
        bf.append("select ");
        for (Field me : t.getDeclaredFields()) {
            bf.append(me.getName());
            bf.append(",");
        }
        bf.delete(bf.length() - 1, bf.length());
        bf.append(" from ");
        bf.append(t.getSimpleName());
        if (slist.size() > 0) {
            bf.append(" where ");
            for (int i = 0; i < slist.size(); i++) {
                bf.append(slist.get(i));
                bf.append(" and ");
            }
            bf.delete(bf.length() - 4, bf.length());
        }
        return bf.toString();
    }

    /**
     * 多表查询 待续
     * @param clist:按照表顺序依次添加的对象集合     list:按照表顺序添加两表内连接字段(例list.get(0), list.get(1)分别存储表一和表2连接字段 2，3存储表二表三)
     * @param slist:条件参数表达式集合合(例:sno>? ; sno=? ; sno in(?,?))
     * @return 多表查询的SQL语句
     */
    @SuppressWarnings("rawtypes")
    public static String createSelectSql(List<Class> clist, List<String> list, List<String> slist) {
        StringBuffer bf = new StringBuffer();
        bf.append("select *");
        bf.append(" from ");
        bf.append(clist.get(0).getSimpleName());
        for (int i = 1; i < clist.size(); i++) {
            bf.append(" inner join ");
            bf.append(clist.get(i).getSimpleName());
            bf.append(" on (");
            bf.append(clist.get(i - 1).getSimpleName());
            bf.append(".");
            bf.append(list.get(i * 2 - 2));
            bf.append("=");
            bf.append(clist.get(i).getSimpleName());
            bf.append(".");
            bf.append(list.get(i * 2 - 1));
            bf.append(") ");
        }
        bf.append("where ");
        handleList2Buffer(bf, slist);
        return bf.toString();
    }

    /**
     * 生成修改语句  修改全部参数
     * @param t:表对应的对象 slist:条件参数表达式集合(例:sno>? ; sno=? ; sno in(?,?))
     * @return 修改Sql语句
     */
    public static <T> String createUpdateSql(Class<T> t, List<String> list, List<String> slist) {
        StringBuffer bf = new StringBuffer();
        bf.append("update ");
        bf.append(t.getSimpleName());
        bf.append(" set ");
        for (String str : list) {
            bf.append(str);
            bf.append("=?,");
        }
        bf.delete(bf.length() - 1, bf.length());
        bf.append(" where ");
        handleList2Buffer(bf, slist);
        return bf.toString();
    }

    /**
     * 生成删除语句
     * @param t:表对应的对象 slist:条件参数表达式集合(例:sno>? ; sno=? ; sno in(?,?))
     * @return 删除Sql语句
     */
    public static <T> String createDeleteSql(Class<T> t, List<String> slist) {
        StringBuffer bf = new StringBuffer();
        bf.append("delete from ");
        bf.append(t.getSimpleName());
        bf.append(" where ");
        handleList2Buffer(bf, slist);
        return bf.toString();
    }

    static void handleResultSet(List<Map<String, Object>> list, ResultSet resultSet) {
        try {
            if (resultSet != null && resultSet.last()) {
                resultSet.beforeFirst();
                ResultSetMetaData rsmd = resultSet.getMetaData();
                while (resultSet.next()) {
                    Map<String, Object> map = Maps.newHashMap();
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        map.put(rsmd.getColumnName(i), resultSet.getString(i));
                    }
                    list.add(map);
                }
                resultSet.beforeFirst();
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void disconnect(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void excuteSql(Connection connection, List<String> list, String sql, Object[] params) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
            }

            if (pstmt.executeUpdate() > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    String pkColumnValue = keys.getString(1);
                    list.add(pkColumnValue);
                }
                keys.beforeFirst();
                keys.close();
            }

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void querySql(Connection connection, List<Map<String, Object>> list, String sql, Object[] params) throws SQLException {
        if (connection == null) {
            throw new QueryDataException("没有建立数据库连接不能进行数据库操作！");
        }
        PreparedStatement pstmt = connection.prepareStatement(sql);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
        ResultSet resultSet = pstmt.executeQuery();
        handleResultSet(list, resultSet);
        pstmt.close();
    }

    @SuppressWarnings("unchecked")
    static <T> List<T> handle2List(T t, List<Map<String, Object>> mapList, List<Request> requestList, List<String> pkValues) {
        if (Map.class.isInstance(t)) {
            List<Map<String, Object>> list = Lists.newArrayList();
            list.addAll(mapList);
            mapList.clear();
            return (List<T>) list;
        } else if (Request.class.isInstance(t)) {
            List<Request> list = Lists.newArrayList();
            list.addAll(requestList);
            requestList.clear();
            return (List<T>) list;
        } else {
            List<String> list = Lists.newArrayList(pkValues);
            pkValues.clear();
            return (List<T>) list;
        }
    }

    private static void handleList2Buffer(StringBuffer buffer, List<String> slist) {
        if (slist.size() > 0) {
            for (String str : slist) {
                buffer.append(str);
                buffer.append(" and ");
            }
            buffer.delete(buffer.length() - 4, buffer.length());
        }
    }

    public void connect2Database(String ip, String databaseName, String dbUser, String dbPassword) throws Exception {
        Class.forName(DRIVER_CLASS_NAME).newInstance();
        String connectString = "jdbc:mysql://" + ip + "/" + databaseName + "?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=round&useSSL=false";
        setConnection(DriverManager.getConnection(connectString, dbUser, dbPassword));
    }

    public void disconnect4Database() throws SQLException {
        if (connectionThreadLocal.get() != null) {
            connectionThreadLocal.get().close();
        }
    }

    public void executeSql(String sqlString) throws SQLException {
        System.out.println("----------------SQL: " + sqlString + "------------------");
        Statement stmt = getConnection().createStatement();
        try {
            stmt.execute(sqlString);
        } finally {
            stmt.close();
        }
    }

    /**
     * 查询数据
     * @param sqlString
     * @return
     * @throws SQLException
     */
    public ResultSet getSqlResult(String sqlString) throws SQLException {
        Statement stmt;
        ResultSet rs;
        try {
            stmt = getConnection().createStatement();
            stmt.execute(sqlString);
            rs = stmt.getResultSet();
        } catch (SQLException e) {
            throw e;
        }
        return rs;
    }

    @Deprecated
    public List<Map<String, Object>> getResultBySql(String sqlString, Object... params) throws SQLException {
        QueryRunner runner = new QueryRunner();
        List<Map<String, Object>> mapList = null;
        try {
            mapList = runner.query(getConnection(), sqlString, new MapListHandler(), params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mapList;
    }

    /**
     * 更新数据
     * @param sqlString
     * @param params
     */
    @Deprecated
    public void updateResultBySql(String sqlString, Object... params) {
        QueryRunner runner = new QueryRunner();
        List<Map<String, Object>> mapList = null;
        try {
            runner.update(getConnection(), sqlString, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        if (connectionThreadLocal.get() == null) {
            throw new IllegalStateException("No connection open. Did you forget to run 'Connect To Database' before?");
        }
        return connectionThreadLocal.get();
    }

    private void setConnection(Connection connection) {
        connectionThreadLocal.set(connection);
    }
}