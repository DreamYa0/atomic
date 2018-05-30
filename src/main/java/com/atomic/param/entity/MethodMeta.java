package com.atomic.param.entity;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * 测试方法的信息类
 */
public class MethodMeta {
    /**
     * 测试类
     */
    private Class testClass;
    /**
     * 测试方法
     */
    private Method testMethod;
    /**
     * 接口类
     */
    private Class interfaceClass;

    /**
     * 接口实例
     */
    private Object interfaceObj;
    /**
     * 接口方法
     */
    private Method interfaceMethod;
    /**
     * 接口定义方法
     */
    private Method declaredInterfaceMethod;
    /**
     * 接口方法入参的实际参数类型
     */
    private Type[] paramTypes;
    /**
     * 接口方法入参的实际参数名称
     */
    private String[] paramNames;
    /**
     * 返回类型
     */
    private Type returnType;
    /**
     * 测试用例数据
     */
    private Map<String, Object> param;
    /**
     * 需要循环遍历的属性名或者Request<Number>型excel中参数名
     */
    private String multiTimeField;
    /**
     * 被测方法名称
     */
    private String methodName;

    public Method getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(Method testMethod) {
        this.testMethod = testMethod;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public Class getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }

    public Method getDeclaredInterfaceMethod() {
        return declaredInterfaceMethod;
    }

    public void setDeclaredInterfaceMethod(Method declaredInterfaceMethod) {
        this.declaredInterfaceMethod = declaredInterfaceMethod;
    }

    public String getMultiTimeField() {
        return multiTimeField;
    }

    public void setMultiTimeField(String multiTimeField) {
        this.multiTimeField = multiTimeField;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }

    public Type[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Type[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Class getTestClass() {
        return testClass;
    }

    public void setTestClass(Class testClass) {
        this.testClass = testClass;
    }

    public Object getInterfaceObj() {
        return interfaceObj;
    }

    public void setInterfaceObj(Object interfaceObj) {
        this.interfaceObj = interfaceObj;
    }

    public Method getInterfaceMethod() {
        return interfaceMethod;
    }

    public void setInterfaceMethod(Method interfaceMethod) {
        this.interfaceMethod = interfaceMethod;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
