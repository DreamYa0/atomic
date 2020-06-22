package com.atomic.tools.mock.mybatis;

import com.atomic.config.TestMethodMode;
import com.atomic.tools.mock.data.MockContext;
import com.atomic.tools.mock.data.MockDataService;
import com.atomic.tools.mock.data.MybatisMockData2FileServiceImpl;
import com.atomic.tools.mock.dto.MockData4Db;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.testng.Reporter;

import java.sql.Statement;
import java.util.Properties;

@Intercepts({@Signature(method = "handleResultSets", type = ResultSetHandler.class, args = {Statement.class})})
public class UnitTestFilter4Mybatis implements Interceptor {

    private MockDataService<MockData4Db, Invocation> mockDataService;

    public UnitTestFilter4Mybatis() {
        mockDataService = MybatisMockData2FileServiceImpl.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object intercept(Invocation invocation) throws Throwable {

        if (MockContext.getContext().getMode() == TestMethodMode.NORMAL) {
            return invocation.proceed();
        }

        if (MockContext.getContext().getMode() == TestMethodMode.REC) {
            Object result = invocation.proceed();


            MockData4Db mockData4Db = new MockData4Db();
            mockData4Db.setDbResult(result);

            mockDataService.insertMockData(mockData4Db);
            return result;
        }

        if (MockContext.getContext().getMode() == TestMethodMode.REPLAY) {

            Object result = mockDataService.getMockData(invocation);
            if (result == null) {
                Reporter.log("{} 调用的方法:{}mock数据不存在" + MockContext.getContext().getTestMethod() +
                        invocation.getTarget());
                throw new RuntimeException("mock数据不存在");
            }
            return result;

        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (MockContext.getContext().getMode() == null) {
            MockContext.getContext().removeContext();
            return target;
        }
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

}
