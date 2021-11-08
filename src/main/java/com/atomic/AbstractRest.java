package com.atomic;

import com.atomic.param.util.DataProviderUtils;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.Iterator;


/**
 * @author dreamyao
 * @title
 * @date  2018/4/21 下午10:05
 * @since 1.0.0
 */
public abstract class AbstractRest {

    @DataProvider(name = "cases")
    public Iterator<Object[]> dataProvider(Method method) throws Exception {
        // 接口数据驱动
         return DataProviderUtils.readDataSource(this, method);
    }

    @DataProvider(name = "parallelCases", parallel = true)
    public Iterator<Object[]> parallelDataProvider(Method method) throws Exception {
        // 接口数据驱动-并行
         return DataProviderUtils.readDataSource(this, method);
    }
}
