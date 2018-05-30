package com.atomic.tools.sql;

import com.atomic.config.CenterConfig;
import com.atomic.config.GlobalConfig;
import jodd.db.pool.CoreConnectionPool;

import java.sql.Connection;
import java.util.Map;

import static com.atomic.param.Constants.*;

/**
 * @author dreamyao
 * @version 1.0.0
 * @description
 * @Data 2018/05/30 10:48
 */
public class ConnectionPool {

    public static synchronized Connection getConnection(String database) {
        //加载环境配置文件
        GlobalConfig.load();
        String profile = GlobalConfig.getProfile();
        Map<String, String> maps = CenterConfig.newInstance().readPropertyConfig(profile);
        String ip = maps.get(JDBC_IP);
        String dbUser = maps.get(JDBC_USER);
        String dbPassword = maps.get(JDBC_PASSWORD);
        CoreConnectionPool connectionPool = new CoreConnectionPool();
        connectionPool.setDriver(DRIVER_CLASS_NAME);
        connectionPool.setUrl("jdbc:mysql://" + ip + "/" + database + "?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=round&useSSL=false");
        connectionPool.setUser(dbUser);
        connectionPool.setPassword(dbPassword);
        connectionPool.setMinConnections(5);
        connectionPool.setMaxConnections(10);
        connectionPool.init();
        return connectionPool.getConnection();
    }
}
