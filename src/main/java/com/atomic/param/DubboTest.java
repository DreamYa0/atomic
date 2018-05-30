package com.atomic.param;

import com.alibaba.fastjson.JSON;
import com.atomic.BaseUnitTest;
import com.atomic.ITestBase;
import com.atomic.annotations.AnnotationUtils;
import com.atomic.exception.ExceptionUtils;
import com.atomic.param.entity.MethodMeta;
import com.atomic.tools.http.HttpRequestProcess;
import com.atomic.util.ReflectionUtils;
import com.coinsuper.common.model.BaseRequest;
import com.coinsuper.common.model.Request;
import com.coinsuper.common.model.Result;
import com.google.common.collect.Maps;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;

import static com.atomic.param.StringUtils.transferMap2Bean;


/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
public abstract class DubboTest<T> extends BaseUnitTest implements ITestBase {

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        // 有Ignore注解，就直接转测试代码
        if (AnnotationUtils.isIgnoreMethod(MethodMetaUtils.getTestMethod(testResult))) {
            super.run(callBack, testResult);
            return;
        }
        if (testResult.getParameters() == null || testResult.getParameters().length == 0) {
            throw new RuntimeException("cannot get param");
        }
        // 获取param
        Map<String, Object> param = ParamUtils.getParamContext(testResult);
        try {
            MethodMetaUtils.getMethodMeta(param, testResult, this);
            startRunTest(param, callBack, testResult);
        } catch (Exception e) {
            handleException(e, param, callBack, testResult);
        }
    }

    private void startRunTest(Map<String, Object> param, IHookCallBack callBack, ITestResult testResult) throws Exception {
        // 先执行beforeTestMethod
        beforeTest(param);
        // 调用方法
        commonTest(testResult);
        // 结果为 Y 才执行断言
        if (ParamUtils.isExpectSuccess(param)) {
            execAssertMethod(param, callBack, testResult);
        }
    }

    private void handleException(Exception e, Map<String, Object> param, IHookCallBack callBack, ITestResult testResult) {
        // 期望结果为 Y 直接抛异常 ；beforeTest里面的异常也直接抛出
        if (ParamUtils.isExpectSuccess(param) || ExceptionUtils.isExceptionThrowsBySpecialMethod(e, "beforeTest")) {
            throw new RuntimeException(e);
        }
        param.put(Constants.RESULT_NAME, ResultAssert.exceptionDeal(e));
        try {
            MethodMeta methodMeta = MethodMetaUtils.getMethodMeta(param, testResult, this);
            ParamPrint.resultPrint(methodMeta.getInterfaceMethod().getName(), param.get(Constants.RESULT_NAME), param, param.get(Constants.PARAMETER_NAME_));
            execAssertMethod(param, callBack, testResult);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    private void execAssertMethod(Map<String, Object> param, IHookCallBack callBack, ITestResult testResult) throws Exception {
        // 注入参数和结果
        TestNGUtils.injectResultAndParameters(param, testResult, this);
        // 转断言
        super.run(callBack, testResult);
    }

    private void commonTest(ITestResult testResult) throws Exception {
        commonTest(testResult, (param, result, parameters) -> param.put(Constants.RESULT_NAME, result));
    }

    private void commonTest(final ITestResult testResult, final ITestResultCallback callback) throws Exception {
        final Map<String, Object> param = ParamUtils.getParamContext(testResult);
        final MethodMeta methodMeta = MethodMetaUtils.getMethodMeta(param, testResult, this);
        // 先判断是不是foreach循环，不是就只执行一次
        execMethodMulitTimes(param.get(methodMeta.getMultiTimeField()), new ITestMethodMultiTimes() {
            @Override
            public void execTestMethod(Object paramValue) throws Exception {
                prepareExecMethod(testResult, paramValue, param, methodMeta, callback);
            }
        });
    }

    private void prepareExecMethod(ITestResult testResult, Object paramValue, Map<String, Object> param, final MethodMeta methodMeta, ITestResultCallback callback) throws Exception {
        // 如果有新值，替换成新值
        Map<String, Object> newParam = Maps.newHashMap(param);
        if (paramValue != null) {
            newParam.put(methodMeta.getMultiTimeField(), paramValue);
        }
        // 构造入参
        final Object[] parameters = generateParameters(methodMeta, newParam);
        param.put(Constants.PARAMETER_NAME_, parameters);
        param.put(Constants.EXCEL_DESC, newParam.get(Constants.EXCEL_DESC));// 备注有可能有额外信息
        boolean isPrintResult = AnnotationUtils.isPrintResult(methodMeta.getTestMethod());
        execMethod(testResult, isPrintResult, methodMeta.getInterfaceMethod(), methodMeta.getReturnType(), param, callback, parameters);
    }

    private void execMethod(ITestResult testResult, boolean isPrintResult, Method method, Type returnType, Map<String, Object> param, ITestResultCallback callback, Object... parameters) throws Exception {
        Object result = dubboTest(testResult);
        if (ParamUtils.isAutoTest(param) && !isPrintResult) {
            System.out.println(method.getName() + "测试用例执行完一个");
        } else {
            ParamPrint.resultPrint(method.getName(), result, param, parameters);
        }
        ResultAssert.assertResult(result, returnType, param, callback, parameters);
    }

    /**
     * 循环执行测试函数
     */
    private void execMethodMulitTimes(Object paramValue, ITestMethodMultiTimes testMethod) throws Exception {
        // 循环遍历
        StringUtils.ForEachClass forEachClass = StringUtils.getForEachClass(paramValue);
        if (forEachClass != null) {
            for (int i = forEachClass.getStart(); i <= forEachClass.getEnd(); i++) {
                testMethod.execTestMethod(String.valueOf(i));
            }
        } else {
            testMethod.execTestMethod(paramValue);
        }
    }

    @SuppressWarnings("unchecked")
    public <R> Result<R> dubboTest(ITestResult testResult) throws Exception {
        // 构造url，类似http://172.26.8.70:8088/api/com.atomic.servicer.manager.service.ban.UserBanService/checkBan?param={%22data%22:18}
        Map<String, Object> param = ParamUtils.getParamContext(testResult);
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(param.get(Constants.EXCEL_IPANDPORT));
        sb.append("/api/");
        // 从Spring获取接口类定义
        Class interfaceType = MethodMetaUtils.getInterfaceClass(this);
        // 获取param
        MethodMeta methodMeta = (MethodMeta) param.get(Constants.TESTMETHODMETA);
        sb.append(interfaceType.getName());
        sb.append("/");
        sb.append(methodMeta.getInterfaceMethod().getName());
        sb.append("?param=");
        Object[] parameters = (Object[]) param.get(Constants.PARAMETER_NAME_);
        String paramJSON = ParamUtils.getJSONStringWithDateFormat(parameters[0], false, null);
        paramJSON = URLEncoder.encode(paramJSON, "UTF-8");// 特殊字符转义
        sb.append(paramJSON);
        String response = HttpRequestProcess.httpGet(sb.toString());
        return (Result<R>) StringUtils.json2Bean("", response, methodMeta.getReturnType());
    }

    /**
     * 使用map自动构造所有入参
     * @param methodMeta
     * @param newParam
     * @return
     * @throws Exception
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    private Object[] generateParameters(final MethodMeta methodMeta, Map<String, Object> newParam) throws Exception {
        final Object[] parameters = new Object[methodMeta.getParamTypes().length];
        for (int i = 0; i < methodMeta.getParamTypes().length; i++) {
            // 实体类，且不是包装类
            if (methodMeta.getParamTypes()[i] instanceof Class) {
                String simpleClassName = ((Class) methodMeta.getParamTypes()[i]).getSimpleName();
                if (!StringUtils.isBasicType(simpleClassName)) {
                    // 先用json转化，不成功再使用属性转化
                    parameters[i] = StringUtils.json2Bean(StringUtils.getValue(newParam.get(methodMeta.getParamNames()[i])), methodMeta.getParamTypes()[i], methodMeta.getParamNames()[i]);
                    if (parameters[i] == null) {
                        Object obj = ReflectionUtils.initFromClass((Class) methodMeta.getParamTypes()[i]);
                        transferMap2Bean(obj, newParam);
                        parameters[i] = obj;
                    }
                } else {
                    parameters[i] = StringUtils.json2Bean(simpleClassName, StringUtils.getValue(newParam.get(methodMeta.getParamNames()[i])), methodMeta.getParamTypes()[i]);
                }
            } else {
                // 泛型
                Type paramType = getParameterType(methodMeta.getDeclaredInterfaceMethod(), i);
                // Request型
                if (ParamUtils.isParamTypeExtendsBaseRequest(methodMeta.getDeclaredInterfaceMethod(), i)) {
                    Type parameterizedType = getParameterizedType(methodMeta.getDeclaredInterfaceMethod(), i);
                    if (parameterizedType instanceof ParameterizedType) {
                        // 泛型按json转
                        parameters[i] = getRequest(parameterizedType, getParamValue(methodMeta, i, newParam));
                    } else {
                        Class paramClass = (Class) parameterizedType;
                        // XXXRequest
                        if (BaseRequest.class.isAssignableFrom(paramClass)) {
                            parameters[i] = getBaseRequest(paramClass.asSubclass(BaseRequest.class), newParam);
                        } else if (StringUtils.isBasicType(paramClass.getSimpleName())) {
                            parameters[i] = getRequest(parameterizedType, getParamValue(methodMeta, i, newParam));
                        } else {
                            // Request<XXXDTO> 或 XXXRequest<XXXDTO>
                            parameters[i] = getRequest(paramType, paramClass, newParam);
                        }
                    }
                } else {
                    // 其他泛型，直接使用JSON转了
                    if (i < methodMeta.getParamNames().length && newParam.get(methodMeta.getParamNames()[i]) != null) {
                        parameters[i] = StringUtils.json2Bean(StringUtils.getValue(newParam.get(methodMeta.getParamNames()[i])), paramType, methodMeta.getParamNames()[i]);
                    }
                }
            }
        }
        return parameters;
    }



    /**
     * 构造Request
     * @param clazz
     * @param valMap
     * @param <T>    XxxRequest
     * @return
     */
    private <T extends BaseRequest> T getBaseRequest(Class<T> clazz, Map<String, Object> valMap) {
        T t = ReflectionUtils.initFromClass(clazz);
        transferMap2Bean(t, valMap);
        return t;
    }

    /**
     * 构造Request
     * @param paramType
     * @param valMap
     * @param <T>       DTO
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> Object getRequest(Type paramType, Class<T> clazz, Map<String, Object> valMap) throws Exception {
        // XXXRequest<T>
        Class requestClass = ((ParameterizedTypeImpl) paramType).getRawType();
        Object request = ReflectionUtils.initFromClass(requestClass);
        T t = ReflectionUtils.initFromClass(clazz);
        // 设置Request本身的属性值
        StringUtils.transferMap2Bean(t, valMap);
        // 设置DTO的属性值
        StringUtils.transferMap2Bean(request, valMap);
        // 查找泛型是T的属性，但是因为被擦除了，类型是Object
        Field field = ReflectionUtils.getField(request, Object.class, "data");
        if (field != null) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(request, t);
        }
        return request;
    }

    /**
     * 构造Request
     * @param type
     * @param value
     * @param <T>   基本类型或者泛型
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> Request<T> getRequest(Type type, String value) throws Exception {
        Request<T> request = new Request<>();
        if (!StringUtils.isExcelValueEmpty(value)) {
            T t;
            if (type instanceof Class) {
                t = (T) StringUtils.json2Bean(((Class) type).getSimpleName(), value, type);
            } else {
                //TODO 如果遇到像1240a9a2-914e-4516-89cb-8653870298fe这样的String会报错
                t = JSON.parseObject(value, type);
            }
            request.setData(t);
        }
        return request;
    }

    private String getParamValue(MethodMeta methodMeta, int index, Map<String, Object> newParam) {
        String param = ParamUtils.getParamName(methodMeta, index);
        return StringUtils.getValue(newParam.get(param));
    }

    /**
     * 获取ParameterizedType
     * @param method     方法
     * @param paramIndex 参数序号
     * @return 参数类型
     * @throws Exception
     */
    private Type getParameterizedType(Method method, int paramIndex) throws Exception {
        return ((ParameterizedType) (method.getGenericParameterTypes()[paramIndex])).getActualTypeArguments()[0];
    }

    /**
     * 获取ParameterType
     * @param method     方法
     * @param paramIndex 参数序号
     * @return 参数类型
     * @throws Exception
     */
    private Type getParameterType(Method method, int paramIndex) throws Exception {
        return method.getGenericParameterTypes()[paramIndex];
    }
}

