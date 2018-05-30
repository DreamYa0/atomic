package com.atomic.tools.mock.data;

/**
 * @author jsy.
 * @title 做个接口，这样以后可以扩展为其他存储方式
 * @time 16/8/8.
 */
public interface MockDataService<T, F> {

    Object getMockData(F invocation);

    void insertMockData(T mockData);

    void deleteMockData();
}
