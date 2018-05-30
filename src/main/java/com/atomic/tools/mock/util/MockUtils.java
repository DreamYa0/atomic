package com.atomic.tools.mock.util;

import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
public class MockUtils {

    /**
     * 把某对象下面的某属性进行mock
     * @param target     待mock对象所属类的实例
     * @param field      需要mock的属性
     * @param mockObject mock对象
     */
    public static void mockObject(Object target, String field, Object mockObject) {
        try {
            ReflectionTestUtils.setField(AopTargetUtils.getTarget(target), field, mockObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
