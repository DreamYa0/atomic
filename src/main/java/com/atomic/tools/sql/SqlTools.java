package com.atomic.tools.sql;

import com.alibaba.fastjson.JSON;
import com.atomic.config.CenterConfig;
import com.atomic.config.GlobalConfig;
import com.atomic.enums.TestMode;
import com.atomic.param.Constants;
import com.google.common.collect.Lists;
import org.assertj.db.type.Request;
import org.assertj.db.type.Source;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Deprecated
public class SqlTools {

    private final List<Map<String, Object>> list = new CopyOnWriteArrayList<>();
    private String ip = "";
    private String database = "";
    private String dbPassword = "";
    private String dbUser = "";
    private SqlUtils sqlUtils = null;
    private String profile;


    public SqlTools(String env) {
        this.profile = env;
        crateSqlTools();
    }

    public SqlTools() {
        GlobalConfig.load();//加载环境配置文件
        profile = GlobalConfig.getProfile();
        crateSqlTools();
    }

    public static void main(String[] args) throws Exception {
        List<Map<String, Object>> list = Lists.newArrayList();
        SqlTools sql = new SqlTools(TestMode.TEST_ONE.getName());
        sql.connect("exchange_liantiao_test");
        // String tableName = "mk_dealreq";
        ResultSet rs = sql.getResult("select * from order_info  order by utc_create desc limit 1");
        // list =  SqlTools.getListFromResultSet(rs);
        System.out.println(JSON.toJSON(list));
        System.out.println(JSON.toJSON(list));
    }

    private void crateSqlTools() {
        Map<String, String> maps = CenterConfig.newInstance().readPropertyConfig(profile);
        ip = maps.get(Constants.JDBC_IP);
        database = maps.get(Constants.JDBC_NAME);
        dbUser = maps.get(Constants.JDBC_USER);
        dbPassword = maps.get(Constants.JDBC_PASSWORD);
        this.connect();
    }

    /**
     * 重新连接数据库
     * @param config 可依次输入database、ip、dbUser、dbPassword 未输入部分,保留默认配置,可用null占位
     */
    public synchronized void connect(String... config) {
        setConfig(config);
        try {
            if (sqlUtils == null) {
                sqlUtils = new SqlUtils();
            } else {
                sqlUtils.disconnect4Database();
                sqlUtils = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (sqlUtils == null) {
                sqlUtils = new SqlUtils();
            }
            sqlUtils.connect2Database(ip, database, dbUser, dbPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setConfig(String... config) {
        if (config == null || config.length == 0) {
            return;
        }
        if (config[0] != null) {
            this.database = config[0];
        }
        if (config.length >= 2 && config[1] != null) {
            this.ip = config[1];
        }
        if (config.length >= 3 && config[2] != null) {
            this.dbUser = config[2];
        }
        if (config.length == 4 && config[3] != null) {
            this.dbPassword = config[3];
        }
    }

    public synchronized void disconnect() {
        if (sqlUtils != null) {
            try {
                sqlUtils.disconnect4Database();
                sqlUtils = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取查询结果的封装对象
     * @param database   数据库名称
     * @param querySql   Sql语句
     * @param parameters Sql语句参数
     * @return 获取查询结果的封装对象
     */
    public synchronized Request getRequest(String database, String querySql, Object... parameters) {
        connect(database);
        String url = "jdbc:mysql://" + ip + "/" + database + "?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=round&useSSL=false";
        Source source = new Source(url, dbUser, dbPassword);
        return new Request(source, querySql, parameters);
    }

    /**
     * 查询数据
     * @param tableName 表名
     * @param maps      查询条件的键值对
     * @param add       附加条件 如: desc,order by XX 等
     * @return
     */
    public ResultSet getResult(String tableName, Map<String, Object> maps, String... add) {
        if (tableName == null) {
            return null;
        }
        if (maps == null || maps.size() == 0) {
            String sqlString = "select * from " + tableName;
            sqlString = add.length != 0 ? sqlString + " " + add[0] : sqlString;
            return getResult(sqlString);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("select * from ").append(tableName).append(" where ");
        Iterator<String> it = maps.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            Object obj = maps.get(name);
            String value = null;
            if (obj instanceof Integer) {
                value = obj + "";
            } else if (obj instanceof String) {
                value = "'" + obj + "'";
            }
            sb.append(name).append("=").append(value).append(" ");
            if (it.hasNext()) {
                sb.append("and ");
            }
        }
        String sqlString = sb.toString();
        if (add != null && add.length != 0) {
            sqlString += " " + add[0];
        }
        try {
            return sqlUtils.getSqlResult(sqlString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询数据
     * @param sqlString sql语句
     * @return
     */
    public ResultSet getResult(String sqlString) {
        try {
            return sqlUtils.getSqlResult(sqlString);
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return null;
    }

    public List<Map<String, Object>> queryForMapList(String sqlString) {
        ResultSet rs = getResult(sqlString);
        return getListFromResultSet(rs);
    }

    public void executeSql(String sqlString) throws SQLException {
        sqlUtils.executeSql(sqlString);
    }

    /**
     * 获取查询的总数据
     * @param rs
     * @return
     */
    public int getResultSetSize(ResultSet rs) {
        if (rs == null) {
            return -1;
        }
        try {
            rs.last();
            int size = rs.getRow();
            rs.beforeFirst();
            return size;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 将ResultSet转换为Map的list
     * 针对不同的sql查询方式
     * @param rs
     * @return
     */
    public List<Map<String, Object>> getListFromResultSet(ResultSet rs) {
        if (rs == null) {
            return null;
        }
        int size = getResultSetSize(rs);
        if (size == 0 || size == -1) {
            return list;
        }
        try {
            rs.beforeFirst();
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                Map map = new HashMap();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    map.put(rsmd.getColumnName(i), rs.getString(i));
                }
                list.add(map);
            }
            rs.beforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }
}
