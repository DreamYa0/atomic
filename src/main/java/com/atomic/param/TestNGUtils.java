package com.atomic.param;

import com.atomic.util.ReflectionUtils;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author dreamyao
 * @Data 2017/5/9
 */
public final class TestNGUtils {

    private TestNGUtils() {
    }

    /**
     * 把参数和结果注入到测试函数
     * @param param
     * @param testResult
     * @throws Exception
     */
    public static void injectResultAndParameters(Map<String, Object> param, ITestResult testResult, Object testInstance) throws Exception {
        Method testMethod = MethodMetaUtils.getTestMethod(testResult);
        int parameters = testMethod.getGenericParameterTypes().length;
        if (parameters > 1) {
            // 获取测试函数的入参名称列表
            String[] paramNames = ReflectionUtils.getMethodParamNames(testMethod);
            // 第一个参数是Map<String,Object> context
            testResult.getParameters()[0] = ParamUtils.getParamContextWithoutExtraInfo(param);
            for (int i = 1; i < parameters; i++) {
                // Type paramType = testMethod.getGenericParameterTypes()[i];
                // 入参根据名字来注入值，如果名字获取失败，则使用类型匹配
                String paramName = i < paramNames.length ? paramNames[i] : null;
                if (paramName != null && paramName.equals("result")) {
                    testResult.getParameters()[i] = param.get(Constants.RESULT_NAME);
                }
                // 现在没有入参了
                /*else {
                    testResult.getParameters()[i] = getParameter(param, paramName, paramType, testInstance);
				}*/
            }
        }
    }

    /**
     * 把参数和结果注入到测试函数，先注入默认值，防止执行时参数不匹配
     * @param map
     * @param testMethod
     * @return
     * @throws Exception
     */
    public static Object[] injectResultAndParametersByDefault(Map<String, Object> map, Method testMethod) throws Exception {
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

    /**
     * 从TestNG - ITestResult获取被测接口测试类名
     * @param testResult
     * @return
     */
    public static String getHttpMethod(ITestResult testResult) {
        return testResult.getTestClass().getRealClass().getSimpleName();
    }
}
