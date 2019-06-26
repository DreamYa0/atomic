package com.atomic.tools.mock.dto;

import com.alibaba.dubbo.rpc.Result;

public class MockData4Rpc {

    private String rpcMethod;

    private String rpcRequest;

    private Result rpcResult;

    private Object apiResult;

    public String getRpcMethod() {
        return rpcMethod;
    }

    public void setRpcMethod(String rpcMethod) {
        this.rpcMethod = rpcMethod;
    }

    public String getRpcRequest() {
        return rpcRequest;
    }

    public void setRpcRequest(String rpcRequest) {
        this.rpcRequest = rpcRequest;
    }

    public Result getRpcResult() {
        return rpcResult;
    }

    public void setRpcResult(Result rpcResult) {
        this.rpcResult = rpcResult;
    }

    public Object getApiResult() {
        return apiResult;
    }

    public void setApiResult(Object apiResult) {
        this.apiResult = apiResult;
    }

}
