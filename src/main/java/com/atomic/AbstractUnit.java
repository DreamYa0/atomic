package com.atomic;

import com.atomic.param.util.DataProviderUtils;
import com.atomic.tools.mock.listener.SoaMockListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.Iterator;


// @ContextConfiguration(locations = {"/test-service.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, SoaMockListener.class})
public abstract class AbstractUnit extends AbstractTestNGSpringContextTests {

    @DataProvider(name = "excel")
    public Iterator<Object[]> dataProvider(Method method) throws Exception {
        return DataProviderUtils.readDataSource(this, method);
    }

    @DataProvider(name = "parallelExcel", parallel = true)
    public Iterator<Object[]> parallelDataProvider(Method method) throws Exception {
        return DataProviderUtils.readDataSource(this, method);
    }
}

