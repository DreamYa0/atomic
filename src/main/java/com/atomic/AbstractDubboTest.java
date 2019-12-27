package com.atomic;

import com.atomic.util.DataProviderUtils;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * @author dreamyao
 * @title
 * @date 2019-12-27 13:03
 * @since 1.0.0
 */
public abstract class AbstractDubboTest {

    @DataProvider(name = "excel")
    public Iterator<Object[]> dataProvider(Method method) throws Exception {
        return readDataForIntegration(method);
    }

    @DataProvider(name = "parallelExcel", parallel = true)
    public Iterator<Object[]> parallelDataProvider(Method method) throws Exception {
        return readDataForIntegration(method);
    }

    private Iterator<Object[]> readDataForIntegration(Method method) throws Exception {
        return DataProviderUtils.readExcel(this, method);
    }
}
