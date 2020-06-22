package com.atomic.param;

import org.testng.IClass;
import org.testng.ITestResult;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author dreamyao
 *         Created by dreamyao on 2017/5/29.
 */
public final class HandleMethodName {

    private HandleMethodName() {
    }

    /**
     * 获取调用接口里的被测方法名
     * @param iTestResult 测试结果上下文
     * @return
     */
    public static String getTestMethodName(ITestResult iTestResult) {
        IClass iClass = iTestResult.getTestClass();
        String testClassName = iClass.getName();
        String[] names = testClassName.split("\\.");
        String slempClassName = names[names.length - 1];
        return cutTestMethodName(slempClassName);
    }

    /**
     * 获取方法名称
     * @param methodName 方法名
     * @return
     */
    private static String cutTestMethodName(String methodName) {
        return StringUtils.lowerFirst(methodName.substring(4));
    }

    /**
     * 从ITestResult中获取被测接口的Class名称
     * @param testResult 测试结果上下文
     * @return
     */
    public static String getTestClassName(ITestResult testResult) {
        IClass iClass = testResult.getTestClass();
        Class<?> superClass = iClass.getRealClass();
        Type superType = superClass.getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            String name = ((ParameterizedType) superType).getActualTypeArguments()[0].toString();
            String[] names = name.split("\\.");
            return names[names.length - 1];
        }
        return "";
    }
}
