package com.atomic;

import java.util.Map;

public interface ITestBase {

    /**
     * db初始化,此方法中初始化的数据无法自动回滚
     */
    void initDb();

    /**
     * 测试前执行，例如:获取数据库中的值,可以用新获取的值替换excel中的值
     * @param context 入参excel
     */
    void beforeTest(Map<String, Object> context);
}
