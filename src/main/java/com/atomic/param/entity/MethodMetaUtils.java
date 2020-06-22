package com.atomic.param.entity;

import com.atomic.exception.GetBeanException;
import com.atomic.param.Constants;
import com.atomic.param.ObjUtils;
import com.atomic.param.ParamUtils;
import com.atomic.util.TestNGUtils;
import com.atomic.util.ApplicationUtils;
import com.atomic.util.ReflectionUtils;
import com.google.common.collect.ImmutableList;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 获取测试所需属性
 * @author dreamyao
 * @version 1.1 Created by dreamyao on 2017/5/9.
 */
public final class MethodMetaUtils {

    private MethodMetaUtils() {
    }

    /**
     * 获取要调用的接口对象
     * @param testInstance 对象实例
     * @return 范型对象<T> 中的T对象
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getInterfaceClass(Object testInstance) {
        Type superType = testInstance.getClass().getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            return (Class<? extends T>) ((ParameterizedType) superType).getActualTypeArguments()[0];
        }
        Reporter.log(testInstance.getClass().getName() + "继承的 XXX<T>缺少类定义T");
        throw new RuntimeException(testInstance.getClass().getName() + "继承 XXX<T>缺少类定义T");
    }

    /**
     * 获取测试所需的属性
     * @param interfaceType  接口的定义
     * @param testMethodName 接口中的方法名
     * @param context        测试入参
     * @return 方法属性对象
     * @throws Exception
     */
    public static MethodMeta getMethodMeta(ITestResult testResult,
                                           Class<?> interfaceType,
                                           String testMethodName,
                                           Map<String, Object> context,
                                           CompletableFuture<Object> future) throws Exception {

        MethodMeta methodMeta = new MethodMeta();
        Type[] types = generateMethodMeta(interfaceType, testMethodName);
        String multiTimeField = getMultiTimeField(context);
        Object interfaceObj = future.get();
        if (interfaceObj == null) {
            throw new GetBeanException("从zookeeper获取被测接口实例失败！");
        }
        Method interfaceMethod = ReflectionUtils.getMethod(interfaceObj.getClass(), testMethodName);
        Assert.assertNotNull(interfaceMethod, String.format("无法获取测试类方法，%s", testMethodName));
        // 得到接口方法定义
        Method declaredInterfaceMethod = ReflectionUtils.getMethod(interfaceType, testMethodName);
        Assert.assertNotNull(declaredInterfaceMethod, String.format("无法获取测试类方法定义，%s", testMethodName));
        ITestNGMethod testNGMethod = testResult.getMethod();
        Method testMethod = testNGMethod.getConstructorOrMethod().getMethod();
        methodMeta.setTestMethod(testMethod);
        methodMeta.setInterfaceMethod(interfaceMethod);
        methodMeta.setInterfaceClass(interfaceType);
        methodMeta.setParamTypes(types);
        methodMeta.setMultiTimeField(multiTimeField);
        methodMeta.setReturnType(declaredInterfaceMethod.getGenericReturnType());
        methodMeta.setParamNames(ReflectionUtils.getMethodParamNames(declaredInterfaceMethod));
        methodMeta.setInterfaceObj(interfaceObj);
        methodMeta.setDeclaredInterfaceMethod(declaredInterfaceMethod);
        methodMeta.setMethodName(testMethodName);
        return methodMeta;
    }

    private static Type[] generateMethodMeta(Class interfaceType, String testMethodName) {
        // 得到接口方法定义
        Method declaredInterfaceMethod = ReflectionUtils.getMethod(interfaceType, testMethodName);
        return declaredInterfaceMethod != null ? declaredInterfaceMethod.getGenericParameterTypes() : new Type[0];
    }

    /**
     * 获取测试所需的一些属性
     * @return 方法属性集合
     * @throws Exception
     */
    public static MethodMeta getMethodMeta(Map<String, Object> context,
                                           ITestResult testResult,
                                           Object testInstance) throws Exception {

        Object object = context.get(Constants.TESTMETHODMETA);
        if (object != null) {
            return (MethodMeta) object;
        }
        MethodMeta methodMeta = generateMethodMeta(testResult, testInstance);
        context.put(Constants.TESTMETHODMETA, methodMeta);
        return methodMeta;
    }

    /**
     * 获取测试所需的一些属性
     * @param param        入参
     * @param testInstance 测试类实例
     * @return 方法属性集合
     * @throws Exception
     */
    public static MethodMeta generateMethodMeta(Map<String, Object> param, Object testInstance) throws Exception {
        // 得到 TestContextManager.testContext 属性值
        TestContext testContext = getTestContext(testInstance);
        Class testClass = testContext.getTestClass();
        return generateMethodMeta(param, testContext.getTestInstance(), testClass, testInstance);
    }

    /**
     * 得到 TestContextManager.testContext 属性值
     * @param testInstance 测试类实例
     * @return TestContext
     * @throws Exception
     */
    private static TestContext getTestContext(Object testInstance) throws Exception {
        TestContextManager testContextManager = getTestContextManager(testInstance);
        TestContext testContext = (TestContext) getFieldValue(testContextManager, TestContextManager.class,
                "testContext");
        Assert.assertNotNull(testContext, "cannot get testContext");
        return testContext;
    }

    /**
     * 得到 AbstractTestNGSpringContextTests.testContextManager 属性值
     * @param testInstance 测试类实例
     * @return TestContextManager
     * @throws Exception
     */
    private static TestContextManager getTestContextManager(Object testInstance) throws Exception {
        TestContextManager testContextManager = (TestContextManager) getFieldValue(testInstance,
                AbstractTestNGSpringContextTests.class, "testContextManager");
        Assert.assertNotNull(testContextManager, "cannot get testContextManager");
        return testContextManager;
    }

    /**
     * 获取属性值
     * @param bean        实例
     * @param targetClass 属性所属class
     * @param fieldName   属性名
     * @return Object
     * @throws IllegalAccessException .{@link IllegalAccessException}
     */
    private static Object getFieldValue(Object bean,
                                        Class<?> targetClass,
                                        String fieldName) throws IllegalAccessException {

        Optional<Field> fieldOptional = Arrays.stream(targetClass.getDeclaredFields())
                .filter(field -> field.getName().equals(fieldName))
                .findFirst();
        if (fieldOptional.isPresent()) {
            fieldOptional.get().setAccessible(true);
            return fieldOptional.get().get(bean);
        }
        return null;
    }

    private static MethodMeta generateMethodMeta(ITestResult testResult, Object testInstance) throws Exception {
        // 截取param
        Map<String, Object> param = TestNGUtils.getParamContext(testResult);
        IClass testClass = testResult.getTestClass();
        return generateMethodMeta(param, testResult.getInstance(), testClass.getRealClass(), testInstance);
    }

    private static MethodMeta generateMethodMeta(Map<String, Object> param,
                                                 Object instances,
                                                 Class<?> testClass,
                                                 Object testInstance) throws Exception {

        String methodName = testClass.getSimpleName().substring(4);
        // 得到测试接口方法名称
        methodName = ParamUtils.lowerFirst(methodName);
        // 从Spring获取接口类定义
        Class<?> interfaceType = getInterfaceClass(testInstance);
        // 得到接口实例
        Object interfaceObj = ApplicationUtils.getBean(interfaceType);
        Assert.assertNotNull(interfaceObj, String.format("无法获取测试类实例，%s", instances.getClass().getName()));

        Method interfaceMethod = ReflectionUtils.getMethod(interfaceObj.getClass(), methodName);
        Assert.assertNotNull(interfaceMethod, String.format("无法获取测试类方法，%s", methodName));

        // 得到接口方法定义
        Method declaredInterfaceMethod = ReflectionUtils.getMethod(interfaceType, methodName);
        Assert.assertNotNull(declaredInterfaceMethod, String.format("无法获取测试类方法定义，%s", methodName));

        // 获取循环遍历的属性名或者Request<Number>型excel中参数名
        String multiTimeField = getMultiTimeField(param);

        MethodMeta methodMeta = new MethodMeta();
        methodMeta.setInterfaceMethod(interfaceMethod);
        methodMeta.setInterfaceObj(interfaceObj);
        methodMeta.setInterfaceClass(interfaceType);
        methodMeta.setTestClass(testClass);
        // 测试方法名称是固定的
        methodMeta.setTestMethod(ReflectionUtils.getMethod(testClass, "testCase"));
        methodMeta.setParamTypes(declaredInterfaceMethod.getGenericParameterTypes());
        methodMeta.setReturnType(declaredInterfaceMethod.getGenericReturnType());
        methodMeta.setParamNames(ReflectionUtils.getMethodParamNames(declaredInterfaceMethod));
        methodMeta.setMultiTimeField(multiTimeField);
        methodMeta.setDeclaredInterfaceMethod(declaredInterfaceMethod);
        return methodMeta;
    }

    /**
     * 获取循环遍历的属性名或者单参数的属性名
     * @param param excel入参
     * @return 字段名称
     * @throws Exception 异常
     */
    private static String getMultiTimeField(Map<String, Object> param) throws Exception {
        for (String key : param.keySet()) {
            ObjUtils.ForEachClass forEachClass = ObjUtils.getForEachClass(param.get(key));
            if (forEachClass != null) {
                return key;
            }
        }
        // Request<Integer>类型的，默认excel中定义为data
        if (param.containsKey(Constants.DEFAULT_SINGLE_PARAM_NAME)) {
            return Constants.DEFAULT_SINGLE_PARAM_NAME;
        }
        ImmutableList<String> expectList = ImmutableList.of(Constants.ASSERT_RESULT,
                Constants.EXCEL_DESC, Constants.AUTO_TEST, Constants.CASE_NAME, Constants.ASSERT_CODE,
                Constants.ASSERT_MSG, Constants.EXPECTED_RESULT, Constants.AUTO_ASSERT, "");

        List<String> allParamList = new ArrayList<>(param.keySet());
        allParamList.removeAll(expectList);
        if (allParamList.size() == 0) {
            return "";
        } else {
            return allParamList.get(0);
        }
    }
}
