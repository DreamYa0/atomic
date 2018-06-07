package com.atomic.util;

import mockit.Expectations;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author dreamyao
 * @title Mock工具类
 * @date 2018/6/7 下午10:00
 * @since 1.0.0
 */
public final class MockUtils {

    private static final Logger logger = LoggerFactory.getLogger(MockUtils.class);

    /**
     * 方法mock
     * @param mockInstance mock对象实列
     * @param mockMethod   mock方法名称
     * @param mockReturn   mock返回结果
     * @param mockParamter mock入参
     */
    @SuppressWarnings("unchecked")
    public static void mock(Object mockInstance, String mockMethod, Object mockReturn, Object... mockParamter) {

        Assert.notNull(mockInstance, "Mock类实列不能为空");
        Assert.notNull(mockMethod, "Mock方法名称不能为空");

        Class mockClass = mockInstance.getClass();

        if (mockClass.isInterface()) {
            // 如果mock的对象为接口时，如mybatis的mapper
            try {
                Method method = mockClass.getMethod(mockMethod);
                new Expectations() {
                    {
                        method.invoke(mockInstance, mockParamter);

                        // 有返回值的方法
                        if (Boolean.FALSE.equals("void".equals(method.getReturnType().getName()))) {
                            result = mockReturn;
                        }
                    }
                };
            } catch (Exception e) {
                logger.error("mock方法失败，mock方法名称为：{}", mockClass.getSimpleName() + "." + mockMethod, e);
            }
        } else {
            // 为Class时
            try {
                Method method = mockClass.getMethod(mockMethod);

                // 获取方法的访问修饰符
                String modifiers = Modifier.toString(method.getModifiers());

                // 判断方法是否为静态方法
                if (modifiers.contains("static")) {
                    // 静态方法mock
                    new Expectations(mockClass) {
                        {
                            MethodUtils.invokeStaticMethod(mockClass, mockMethod, mockParamter);

                            // 有返回值的方法
                            if (Boolean.FALSE.equals("void".equals(method.getReturnType().getName()))) {
                                result = mockReturn;
                            }
                        }
                    };

                } else {
                    // 非静态方法mock（包括普通方法、final方法）
                    new Expectations(mockInstance) {
                        {
                            MethodUtils.invokeMethod(mockInstance, mockMethod, mockParamter);

                            // 有返回值的方法
                            if (Boolean.FALSE.equals("void".equals(method.getReturnType().getName()))) {
                                result = mockReturn;
                            }
                        }
                    };
                }

            } catch (Exception e) {
                logger.error("mock方法失败，mock方法名称为：{}", mockClass.getSimpleName() + "." + mockMethod, e);
            }
        }
    }
}
