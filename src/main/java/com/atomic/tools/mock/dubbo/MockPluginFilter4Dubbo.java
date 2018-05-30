package com.atomic.tools.mock.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2018/05/30 10:48
 */
@Activate(group = Constants.CONSUMER)
public class MockPluginFilter4Dubbo implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String interfaceName = invocation.getInvoker().getInterface().getName();
        String methodName = invocation.getMethodName();
        return null;
    }
}
