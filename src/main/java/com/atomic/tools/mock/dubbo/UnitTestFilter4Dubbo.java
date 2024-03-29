package com.atomic.tools.mock.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.atomic.tools.mock.data.DubboMockData2FileServiceImpl;
import com.atomic.tools.mock.data.MockContext;
import com.atomic.tools.mock.data.MockDataService;
import com.atomic.tools.mock.data.TestMethodMode;
import com.atomic.tools.mock.dto.MockData4Rpc;
import com.atomic.util.GsonUtils;
import org.testng.Reporter;

@Activate(group = Constants.CONSUMER)
public class UnitTestFilter4Dubbo implements Filter {

    private MockDataService<MockData4Rpc,Invocation> mockDataService;

    public UnitTestFilter4Dubbo() {
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
            mockData4Rpc.setRpcRequest(GsonUtils.getGson().toJson(invocation.getArguments()));
            mockData4Rpc.setApiResult(result.getValue());
            mockData4Rpc.setRpcMethod(invocation.getInvoker().getInterface().getName() + ":" +
                    invocation.getMethodName());
            mockData4Rpc.setAttachments(result.getAttachments());
            mockDataService.insertMockData(mockData4Rpc);

            return result;
        }

        //重放，从数据库中取
        if (MockContext.getContext().getMode() == TestMethodMode.REPLAY) {
            Result result = (Result) mockDataService.getMockData(invocation);
            if (result == null) {
                Reporter.log(String.format("调用的方法: %s mock数据不存在",
                        invocation.getInvoker().getInterface() + "." + invocation.getMethodName()));
                throw new RuntimeException("mock数据不存在");
            }
            return result;
        }

        MockContext.getContext().removeContext();
        return invoker.invoke(invocation);
    }
}
