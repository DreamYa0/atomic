package com.atomic;

import com.atomic.param.util.DataProviderUtils;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * @author dreamyao
 * @title
 * @date 2019-12-27 13:03
 * @since 1.0.0
 */
public abstract class AbstractDubbo {

    // @DataProvider(name = "excel")
    @DataProvider(name = "cases")
    public Iterator<Object[]> dataProvider(Method method) throws Exception {
        return DataProviderUtils.readDataSource(this, method);
    }

    // @DataProvider(name = "parallelExcel", parallel = true)
    @DataProvider(name = "parallelCases", parallel = true)
    public Iterator<Object[]> parallelDataProvider(Method method) throws Exception {
        return DataProviderUtils.readDataSource(this, method);
    }
}
