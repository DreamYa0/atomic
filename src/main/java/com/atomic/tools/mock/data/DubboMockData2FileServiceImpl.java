package com.atomic.tools.mock.data;

import com.atomic.param.StringUtils;
import com.atomic.tools.mock.dto.MockData;
import com.atomic.tools.mock.dto.MockData4Rpc;
import com.atomic.tools.mock.helper.MockFileHelper;
import com.atomic.tools.mock.util.JacksonUtils;
import com.atomic.util.FileUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcResult;

import java.lang.reflect.Method;

/**
 * @author jsy.
 * @title
 * @time 16/9/28.
 */
public class DubboMockData2FileServiceImpl implements MockDataService<MockData4Rpc, Invocation> {

    private static final MockDataService instance = new DubboMockData2FileServiceImpl();

    //*********单例模式****************
    private DubboMockData2FileServiceImpl() {

    }

    public static MockDataService<MockData4Rpc, Invocation> getInstance() {
        return instance;
    }

    @Override
    public Object getMockData(Invocation invocation) {

        MockData mockData = MockContext.getContext().getMockData();

        //根据序号取值，order从1开始，数组从0开始，所以减1
        Result result = mockData.getRpcData().get(MockContext.getContext().getRpcOrderAndIncrease() - 1);
        RpcResult ret = (RpcResult) JacksonUtils.decode(JacksonUtils.encode(result), RpcResult.class);
        try {
            Method method = invocation.getInvoker().getInterface()
                    .getMethod(invocation.getMethodName(), invocation.getParameterTypes());
            Object apiResult = StringUtils.json2Bean("", JacksonUtils.encode(ret.getValue()),
                    method.getGenericReturnType());
            ret.setValue(apiResult);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ret;
    }

    @Override
    public void insertMockData(MockData4Rpc mockData) {
        MockContext.getContext().getMockData().getRpcData().add((RpcResult) mockData.getRpcResult());

    }

    @Override
    public void deleteMockData() {
        FileUtils.removeDir(MockFileHelper.getMockFile(MockContext.getContext().getCaseIndex()));
    }
}
