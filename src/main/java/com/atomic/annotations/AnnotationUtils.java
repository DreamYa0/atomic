package com.atomic.annotations;

import com.atomic.exception.AnnotationException;
import com.atomic.param.Constants;
import com.atomic.tools.autotest.AutoTest;
import com.atomic.tools.autotest.AutoTestMode;
import com.atomic.tools.rollback.RollBack;
import com.atomic.tools.rollback.RollBackAll;
import com.atomic.util.TestNGUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 通用注解工具类
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/28.
 */
public abstract class AnnotationUtils {

    /**
     * 判断测试方法是否添加回滚注解
     * @param testMethod
     * @return
     */
    public static boolean isRollBackMethod(Method testMethod) throws AnnotationException {
        Annotation[] annotations = testMethod.getAnnotations();
        if (annotations != null) {
            RollBack rollBack = testMethod.getAnnotation(RollBack.class);
            RollBackAll rollBackAll = testMethod.getAnnotation(RollBackAll.class);
            if (rollBack == null) {
                return false;
            } else if (rollBackAll != null) {
                Reporter.log("不能同时存在两个类型的数据回滚注解！");
                throw new AnnotationException("不能同时存在两个类型的数据回滚注解！");
            } else if (rollBack.enabled()) {
                if ("".equals(rollBack.dbName()) || rollBack.tableName().length == 0) {
                    return false;
                }
            } else if (!rollBack.enabled()) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 检查方法上是否有RollBackAll注解和注解是否开启
     * @param method
     * @return
     */
    public static boolean isRollBackAllMethod(Method method) throws AnnotationException {
        Annotation[] annotations = method.getAnnotations();
        if (annotations != null) {
            RollBack rollBack = method.getAnnotation(RollBack.class);
            RollBackAll rollBackAll = method.getAnnotation(RollBackAll.class);
            if (rollBackAll != null) {
                if (rollBack != null) {
                    Reporter.log("不能同时存在两个类型的数据回滚注解！");
                    throw new AnnotationException("不能同时存在两个类型的数据回滚注解！");
                } else if (!rollBackAll.enabled()) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 处理多库多表时，表名和库名,参数示例：zhubajie_market.mk_service
     * @param method
     * @return
     */
    public static Multimap<String, String> getDbNameAndTableName(Method method) throws AnnotationException {
        RollBackAll rollBackAll = method.getAnnotation(RollBackAll.class);
        String[] dbAndTable = rollBackAll.dbAndTable();
        Multimap<String, String> multimap = ArrayListMultimap.create();
        for (int i = 0; i < dbAndTable.length; i++) {
            String[] dbNameAndTableName = dbAndTable[i].split("\\.");
            if (dbNameAndTableName.length != 2) {
                Reporter.log("传入的库名和表名方式错误！");
                throw new AnnotationException("传入的库名和表名方式错误！");
            }
            multimap.put(dbNameAndTableName[0], dbNameAndTableName[1]);
        }
        return multimap;
    }

    /**
     * 从注解中获取数据库名
     * @param testMethod
     * @return
     */
    public static String getDbName(Method testMethod) {
        RollBack annotation = testMethod.getAnnotation(RollBack.class);
        return annotation.dbName();
    }

    /**
     * 从注解中获取表名
     * @param testMethod
     * @return
     */
    public static String[] getTableName(Method testMethod) {
        RollBack annotation = testMethod.getAnnotation(RollBack.class);
        return annotation.tableName();
    }

    /**
     * 判断@AutoTest注解是否存在
     * @param method
     * @return
     */
    public static boolean isAutoTest(Method method) {
        Annotation[] annotations = method.getAnnotations();
        if (annotations != null) {
            AutoTest autoTest = method.getAnnotation(AutoTest.class);
            if (autoTest == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取自动化测试最大用例限制数
     * @param testMethod
     * @return
     */
    public static int getMaxTestCases(Method testMethod) {
        AutoTest annotation = testMethod.getAnnotation(AutoTest.class);
        if (annotation != null) {
            return annotation.maxTestCases();
        }
        return Constants.MAX_TEST_CASES;
    }

    /**
     * 自动化测试，属性组合模式，默认单属性模式
     * @param testMethod
     * @return
     */
    public static AutoTestMode getAutoTestMode(Method testMethod) {
        AutoTest annotation = testMethod.getAnnotation(AutoTest.class);
        if (annotation != null) {
            return annotation.autoTestMode();
        }
        return AutoTestMode.SINGLE;
    }

    /**
     * 是否打印入参和出参
     * @param testMethod
     * @return
     */
    public static boolean isPrintResult(Method testMethod) {
        AutoTest annotation = testMethod.getAnnotation(AutoTest.class);
        return annotation == null || annotation.autoTestPrintResult();
    }

    /**
     * 判断是否是场景测试
     * @param method
     * @return
     */
    public static boolean isScenario(Method method) {
        Annotation[] annotations = method.getAnnotations();
        if (annotations != null) {
            Scenario scenario = method.getAnnotation(Scenario.class);
            if (scenario == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean isIgnoreMethod(Method testMethod) {
        Annotation[] annotations = testMethod.getAnnotations();
        if (annotations != null) {
            Ignore annotation = testMethod.getAnnotation(Ignore.class);
            return annotation != null;
        }
        return false;
    }

    /**
     * 根据名称获取Bean
     * @param interfaceClass 接口的Class对象
     * @return 接口名称
     */
    public static String getBeanName(Class<?> interfaceClass) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {

        Annotation[] annotations = interfaceClass.getAnnotations();
        if (!ArrayUtils.isEmpty(annotations)) {
            List<Class<? extends Annotation>> serviceList = Arrays.asList(Service.class,
                    Component.class, Controller.class, Repository.class);
            for (Annotation annotation : annotations) {
                if (serviceList.contains(annotation.annotationType())) {
                    Method m = annotation.getClass().getDeclaredMethod("value", null);
                    return (String) m.invoke(annotation);
                }
            }
        }
        return null;
    }

    /**
     * 获取@Test中的依赖方法名称
     * @param testResult
     * @return
     */
    public static String[] getDependsOnMethods(ITestResult testResult) {
        Method method = TestNGUtils.getTestMethod(testResult);
        Test test = method.getAnnotation(Test.class);
        String[] testNames = test.dependsOnMethods();
        if (testNames.length > 0) {
            String[] relyName = new String[testNames.length];
            for (int i = 0; i < testNames.length; i++) {
                String[] names = testNames[i].split("\\.");
                relyName[i] = names[names.length - 2];
            }
            return relyName;
        }
        return null;
    }

    /**
     * 判断@ServicesVersion注解是否存在
     * @param method
     * @return
     */
    public static boolean isServiceVersion(Method method) {
        Annotation[] annotations = method.getAnnotations();
        if (annotations != null) {
            ServiceVersion serviceVersion = method.getAnnotation(ServiceVersion.class);
            if (serviceVersion == null) {
                return false;
            }
            if ("".equals(serviceVersion.version())) {
                Reporter.log("dubbo服务版本号不能为空！");
                throw new AnnotationException("dubbo服务版本号不能为空！");
            }
        }
        return true;
    }

    /**
     * 获取dubbo版本号
     * @param method
     * @return
     */
    public static String getServiceVersion(Method method) {
        ServiceVersion serviceVersion = method.getAnnotation(ServiceVersion.class);
        return serviceVersion.version();
    }
}
