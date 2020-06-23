package com.atomic.tools.mock.data;

import com.atomic.tools.mock.dto.MockData;

/**
 * @author dreamyao
 * @title
 * @date  16/8/11.
 */
public class MockContext {

    private static final ThreadLocal<MockContext> LOCAL = ThreadLocal.withInitial(MockContext::new);
    /**
     * 测试方法
     */
    private String testMethod;
    /**
     * 测试类
     */
    private String testClass;
    /**
     * 外部调用顺序
     */
    private int dbOrder;
    /**
     * 外部调用顺序
     */
    private int rpcOrder;
    /**
     * 测试用例的顺序
     */
    private int caseIndex;
    /**
     * 该方法模式
     */
    private TestMethodMode mode;
    /**
     * mock数据
     */
    private MockData mockData = new MockData();

    private MockContext() {
    }

    public static MockContext getContext() {
        return LOCAL.get();
    }

    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    public void removeContext() {
        LOCAL.remove();
    }

    public int getRpcOrderAndIncrease() {
        return rpcOrder++;
    }

    public int getDbOrderAndIncrease() {
        return dbOrder++;
    }


    public int getDbOrder() {
        return dbOrder;
    }

    public void setDbOrder(int dbOrder) {
        this.dbOrder = dbOrder;
    }

    public int getRpcOrder() {
        return rpcOrder;
    }

    public void setRpcOrder(int rpcOrder) {
        this.rpcOrder = rpcOrder;
    }

    public String getTestClass() {
        return testClass;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public TestMethodMode getMode() {
        return mode;
    }

    public void setMode(TestMethodMode mode) {
        this.mode = mode;
    }

    public int getCaseIndex() {
        return caseIndex;
    }

    public void setCaseIndex(int caseIndex) {
        this.caseIndex = caseIndex;
    }

    public MockData getMockData() {
        return mockData;
    }

    public void setMockData(MockData mockData) {
        this.mockData = mockData;
    }


}
