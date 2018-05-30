package com.atomic.param;

/**
 * @author dreamyao
 * @version 1.1.0 Created by dreamyao on 2017/5/29.
 */
public final class Constants {

    /** 期望结果，为 Y 时才执行断言 */
    public static final String ASSERTRESULT_NAME = "assertResult";

    /** 测试用例标题 */
    public static final String CASE_NAME = "caseName";

    /** 为 Y 时启动自动化测试 */
    public static final String AUTOTEST_NAME = "autotest";

    /** 当期望结果为 N 时才执行assertCode断言 */
    public static final String ASSERTRESULT_CODE = "assertCode";

    /** 当期望结果为 N 时才执行assertDescription断言 */
    public static final String ASSERTRESULT_MSG = "assertMsg";

    /** 预期结果字段，当预期结果内容不为空是进行自动断言 */
    public static final String EXPECTED_RESULT = "expectedResult";

    /** 智能化断言标示，为Y时表示使用智能化断言 */
    public static final String AUTO_ASSERT = "autoAssert";

    /** 请求类型，如：GET,POST */
    public static final String HTTP_MODE = "httpMode";

    /** 请求IP+端口号或域名 */
    public static final String HTTP_HOST = "httpHost";

    /** 请求URI路径 */
    public static final String HTTP_METHOD = "httpMethod";

    /** ContentType */
    public static final String HTTP_CONTENT_TYPE = "httpContentType";

    /** POST JSON提交 */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /** POST表单提交 */
    public static final String CONTENT_TYPE_FROM = "application/x-www-form-urlencoded";

    /** 请求头 */
    public static final String HTTP_HEADER = "httpHeader";

    /** 快捷登陆地址 */
    public static final String LOGIN_URL = "loginUrl";

    /** HTTP GET请求 */
    public static final String HTTP_GET = "GET";

    /** HTTP POST请求 */
    public static final String HTTP_POST = "POST";

    /** 单参数，比如Request<Integer>时，excel中的默认属性名称 */
    public static final String DEFAULT_SINGLE_PARAM_NAME = "data";

    /** Http接口返回类型标识，默认返回String */
    public static final String MESSAGE_TYPE = "messageType";

    /** 接口返回值对象 */
    public static final String RESULT_NAME = "m_result";

    /** 接口入参对象 */
    public static final String PARAMETER_NAME_ = "m_parameter_";

    /** excel中Y字段值 */
    public static final String EXCEL_YES = "Y";

    /** excel中N字段值 */
    public static final String EXCEL_NO = "N";

    /** excel中null字段值 */
    public static final String EXCEL_NULL = "null";

    /** excel中foreach字段值 */
    public static final String EXCEL_FOREACH = "foreach";

    /** excel中random字段值 */
    public static final String EXCEL_RANDOM = "random";

    /** 断言的时候，旧值是执行接口前的数据库值，需要先保存 */
    public static final String ASSERT_ITEM_ = "m_assertItem_";

    /** 断言的时候，旧值是执行接口前的数据库值，需要先保存SQL，以供打印 */
    public static final String OLD_SQL_ = "m_old_sql_";

    /** 测试用例的说明，比如参数type无效 */
    public static final String EXCEL_DESC = "err";

    /** testmethodmeta对象 */
    public static final String TESTMETHODMETA = "testmethodmeta";

    /** ip和端口 */
    public static final String EXCEL_IPANDPORT = "ipPort";

    /** 测试用例的序号 */
    public static final String CASE_INDEX = "CASE_INDEX";

    /** 如果有测试用例此标志设置为Y，则单独运行，其他没带此标志的测试用例跳过 */
    public static final String TEST_ONLY = "testOnly";

    /** 如果有测试用例设置此值，则开启多线程同时调用此测试用例，测试并发 */
    public static final String THREAD_COUNT = "threadCount";

    /** 自动化测试生成用例最大数量默认值 */
    public static final int MAX_TEST_CASES = 100000;

    /** 对应测试用例依赖接口返回结果的序号 */
    public static final String DEPENDENCY_INDEX = "dependencyIndex";

    /** 预期值sql语句 */
    public static final String ASSERT_SQL = "assertSql";

    /** Jira名称 */
    public static final String JIRA_NAME = "jiraName";

    /** Jira标签 */
    public static final String LABELS = "labels";

    /** Jira bug受理人 */
    public static final String ASSIGNEE = "assignee";

    /** Issue 类型 */
    public static final String ISSUE_TYPE = "issueType";

    /** Jira bug等级 */
    public static final String PRIORITY = "priority";

    /** 运行指定测试包 */
    public static final String RUN_TEST_PACKAGES = "run.test.packages";

    /** 运行指定测试分组 */
    public static final String RUN_TEST_GROUPS = "run.test.groups";

    /** 运行指定测试方法 */
    public static final String RUN_TEST_CLASSES = "run.test.classes";

    /** 数据库IP */
    public static final String JDBC_IP = "jdbc.ip";

    /** 数据库账号 */
    public static final String JDBC_USER = "jdbc.user";

    /** 数据库密码 */
    public static final String JDBC_PASSWORD = "jdbc.password";

    /** 数据库名称 */
    public static final String JDBC_NAME = "jdbc.name";

    /** MongoDB数据库IP */
    public static final String MONGODB_IP = "mongodb.ip";

    /** MongoDB数据库账号 */
    public static final String MONGODB_USER = "mongodb.user";

    /** MongoDB数据库密码 */
    public static final String MONGODB_PASSWORD = "mongodb.password";

    /** MySql驱动名称 */
    public static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

    /** zookeeper URL地址 */
    public static final String ZOOKEEPER = "zookeeper";

    /** 预发布环境HTTP代理 */
    public static final String HTTP_PROXY = "http.proxy";

    /** excel中表示身份证号标示 */
    public static final String ID_CARD = "${card}";

    /** excel中表示邮箱号标示 */
    public static final String EMAIL = "${email}";

    /** excel中表示手机号标示 */
    public static final String PHONE_NO = "${phone}";

    /** excel中表示当前时间标示 */
    public static final String NOW_DAY = "${now()}";

    /** 预防布环境 */
    public static final String PROFILE_TESTING = "testing";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
}
