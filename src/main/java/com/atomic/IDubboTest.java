package com.atomic;

import java.util.Map;

/**
 * 采用http请求调用dubbo服务标准测试模版接口
 * @author dreamyao
 */
public interface IDubboTest {

    /**
     * db初始化,此方法中初始化的数据无法自动回滚
     */
    void initDb();

    /**
     * 测试前执行，例如:获取数据库中的值,可以用新获取的值替换excel中的值
     * @param context
     */
    void beforeTest(Map<String, Object> context);
}
