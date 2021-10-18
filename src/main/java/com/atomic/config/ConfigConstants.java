package com.atomic.config;

/**
 * @author dreamyao
 * @title
 * @date 2021/10/18 10:53 上午
 * @since 1.0.0
 */
public class ConfigConstants {

    /**
     * 项目名称
     */
    public static final String PROJECT_NAME = "project.name";
    /**
     * 执行人
     */
    public static final String RUNNER = "runner";
    /**
     * dubbo服务提供者地址
     */
    public static final String DUBBO_PROVIDER_HOST = "dubbo.provider.host";
    /**
     * dubbo服务版本
     */
    public static final String DUBBO_SERVICE_VERSION = "dubbo.service.version";
    /**
     * dubbo服务组
     */
    public static final String DUBBO_SERVICE_GROUP = "dubbo.service.group";
    /**
     * dubbo zk地址
     */
    public static final String DUBBO_ZOOKEEPER = "dubbo.zookeeper";
    /**
     * http 请求地址
     */
    public static final String HTTP_HOST = "http.host";
    /**
     * http 请求header
     */
    public static final String HTTP_HEADER = "http.header";
    /**
     * 数据库地址
     */
    public static final String DATABASE_URL = "database.url";
    /**
     * 数据库用户名
     */
    public static final String DATABASE_USER_NAME = "database.username";
    /**
     * 数据库密码
     */
    public static final String DATABASE_PASSWORD = "database.password";
    /**
     * 运行指定测试包
     */
    public static final String RUN_TEST_PACKAGES = "run.test.packages";
    /**
     * 运行指定测试分组
     */
    public static final String RUN_TEST_GROUPS = "run.test.groups";
    /**
     * 运行指定测试方法
     */
    public static final String RUN_TEST_CLASSES = "run.test.classes";
}
