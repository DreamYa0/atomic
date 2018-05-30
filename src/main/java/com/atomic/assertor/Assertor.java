package com.atomic.assertor;

import java.util.Map;

/**
 * @author dreamyao
 * @title 通用断言接口
 * @Data 2018/5/19 下午8:47
 * @since 1.0.0
 */
public interface Assertor {

    /**
     * 结果断言
     * @param result 返回结果
     * @param context 入参集合
     */
    void assertResult(Object result, Map<String, Object> context);

}
