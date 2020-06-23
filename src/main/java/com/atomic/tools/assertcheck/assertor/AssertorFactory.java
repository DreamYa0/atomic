package com.atomic.tools.assertcheck.assertor;

/**
 * @author dreamyao
 * @title 断言工厂类
 * @date 2018/5/19 下午9:52
 * @since 1.0.0
 */
public class AssertorFactory {

    public static <T extends Assertor> T getAssertor(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
