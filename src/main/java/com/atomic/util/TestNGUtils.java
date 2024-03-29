package com.atomic.util;

import com.atomic.annotations.AnnotationUtils;
import com.atomic.param.Constants;
import com.atomic.param.util.ParamUtils;
import com.atomic.param.util.ReflectionUtils;
import com.atomic.param.ResultCache;
import org.testng.IClass;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author dreamyao
 * @date 2017/5/9
 */
public final class TestNGUtils {

    private TestNGUtils() {
    }

    public static void injectResultAndParameters(Map<String, Object> context,
                                                 ITestResult testResult,
                                                 Object testInstance) throws Exception {
        // 把参数和结果注入到测试函数
        Method testMethod = TestNGUtils.getTestMethod(testResult);
        int parameters = testMethod.getGenericParameterTypes().length;
        if (parameters > 1) {
            // 获取测试函数的入参名称列表
            String[] paramNames = ReflectionUtils.getMethodParamNames(testMethod);
            // 第一个参数是Map<String,Object> context
            testResult.getParameters()[0] = ParamUtils.getParamContextWithoutExtraInfo(context);
            for (int i = 1; i < parameters; i++) {
                // Type paramType = testMethod.getGenericParameterTypes()[i];
                // 入参根据名字来注入值，如果名字获取失败，则使用类型匹配
                String paramName = i < paramNames.length ? paramNames[i] : null;
                if (paramName != null && paramName.equals("result")) {
                    testResult.getParameters()[i] = context.get(Constants.RESULT_NAME);
                }
                // 现在没有入参了
                /*else {
                    testResult.getParameters()[i] = getParameter(context, paramName, paramType, testInstance);
				}*/
            }
        }
    }

    public static Object[] injectResultAndParametersByDefault(Map<String, Object> map,
                                                              Method testMethod) throws Exception {
        // 把参数和结果注入到测试函数，先注入默认值，防止执行时参数不匹配
        int parameters = testMethod.getGenericParameterTypes().length;
        Object[] objects;
        if (parameters > 1) {
            objects = new Object[parameters];
            objects[0] = map;
            // 第一个参数是Map<String,Object> context，不处理；第二个参数是Object result
            for (int i = 1; i < parameters; i++) {
                objects[i] = null;
                // 基本类型要重新赋值
                /*if (testMethod.getGenericParameterTypes()[i] instanceof Class) {
                    Class paramClass = (Class) testMethod.getGenericParameterTypes()[i];
					if (paramClass.isPrimitive()) {
						objects[i] = StringUtils.json2Bean(paramClass.getSimpleName(), "0", null);
					}
				}*/
            }
        } else {
            objects = new Object[]{map};
        }
        return objects;
    }

    public static String getHttpMethod(ITestResult testResult) {
        // 从TestNG - ITestResult获取被测接口测试类名
        return testResult.getTestClass().getRealClass().getSimpleName();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getParamContext(ITestResult testResult) {
        // 从测试上下文中获取入参
        return (Map<String, Object>) testResult.getParameters()[0];
    }

    public static void setParamContext(ITestResult testResult, Map<String, Object> context) {
        // 把处理后的参数从新设置回测试上下文中
        testResult.getParameters()[0] = context;
    }

    public static void injectScenarioReturnResult(ITestResult iTestResult, Map<String, Object> context) {
        // 注入场景测试所需要的依赖方法的返回结果
        //获取接口所依赖的返回值并注入到context中
        String[] dependsOnMethodNames = AnnotationUtils.getDependsOnMethods(iTestResult);
        if (dependsOnMethodNames != null && dependsOnMethodNames.length > 0) {
            for (String dependsOnMethodName : dependsOnMethodNames) {
                Object dependMethodReturn;
                if (ParamUtils.isDependencyIndexNoNull(context)) {
                    dependMethodReturn = ResultCache.getTestResultInCache(dependsOnMethodName,
                            context.get(Constants.DEPENDENCY_INDEX));
                } else {
                    dependMethodReturn = ResultCache.getTestResultInCache(dependsOnMethodName,
                            context.get(Constants.CASE_INDEX));
                }
                if (dependMethodReturn != null) {
                    context.put(dependsOnMethodName, dependMethodReturn);
                }
            }
        }
    }

    public static Method getTestMethod(ITestResult testResult) {
        // 获取测试方法
        return testResult.getMethod().getConstructorOrMethod().getMethod();
    }

    public static String getTestCaseClassName(ITestResult testResult) {
        // 获取测试用例的Class名称(非全限定名称)
        IClass iClass = testResult.getTestClass();
        String testClassName = iClass.getName();
        String[] names = testClassName.split("\\.");
        return names[names.length - 1];
    }
}
