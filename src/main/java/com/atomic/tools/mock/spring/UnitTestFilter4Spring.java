package com.atomic.tools.mock.spring;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.atomic.config.TestMethodMode;
import com.atomic.tools.mock.data.DubboMockData2FileServiceImpl;
import com.atomic.tools.mock.data.MockContext;
import com.atomic.tools.mock.data.MockDataService;
import com.atomic.tools.mock.dto.MockData4Rpc;
import com.atomic.tools.mock.util.JacksonUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.testng.Reporter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Activate(group = Constants.CONSUMER)
public class UnitTestFilter4Spring implements Filter,HandlerInterceptor {

    private MockDataService mockDataService;

    public UnitTestFilter4Spring() {
        mockDataService = DubboMockData2FileServiceImpl.getInstance();
    }

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        if ("com.alibaba.dubbo.monitor.MonitorService".equals(invoker.getInterface().getName())) {
            return invoker.invoke(invocation);
        }

        //正常模式
        if (MockContext.getContext().getMode() == TestMethodMode.NORMAL) {
            return invoker.invoke(invocation);
        }

        //录制模式，录制数据到DB
        if (MockContext.getContext().getMode() == TestMethodMode.REC) {
            Result result = invoker.invoke(invocation);

            MockData4Rpc mockData4Rpc = new MockData4Rpc();
            mockData4Rpc.setRpcRequest(JacksonUtils.encode(invocation.getArguments()));
            mockData4Rpc.setApiResult(result.getValue());

            mockData4Rpc.setRpcResult(result);
            mockData4Rpc.setRpcMethod(invocation.getInvoker().getInterface() + ":" + invocation.getMethodName());
            mockDataService.insertMockData(mockData4Rpc);

            return result;
        }

        //重放，从数据库中取
        if (MockContext.getContext().getMode() == TestMethodMode.REPLAY) {
            Result result = (Result) mockDataService.getMockData(invocation);
            if (result == null) {
                Reporter.log("{} 调用的方法:{}mock数据不存在" + MockContext.getContext().getTestMethod() + invocation.getInvoker().getInterface() + ":" + invocation.getMethodName());
                throw new RuntimeException("mock数据不存在");
            }
            return result;
        }

        MockContext.getContext().removeContext();
        return invoker.invoke(invocation);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
