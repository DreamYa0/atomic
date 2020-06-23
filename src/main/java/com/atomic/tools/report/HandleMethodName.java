package com.atomic.tools.report;

import com.atomic.param.ParamUtils;
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

    public static String getTestMethodName(ITestResult iTestResult) {
        // 获取调用接口里的被测方法名
        IClass iClass = iTestResult.getTestClass();
        String testClassName = iClass.getName();
        String[] names = testClassName.split("\\.");
        String slempClassName = names[names.length - 1];
        return cutTestMethodName(slempClassName);
    }

    private static String cutTestMethodName(String methodName) {
        // 获取方法名称
        return ParamUtils.lowerFirst(methodName.substring(4));
    }

    public static String getTestClassName(ITestResult testResult) {
        // 从ITestResult中获取被测接口的Class名称
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
