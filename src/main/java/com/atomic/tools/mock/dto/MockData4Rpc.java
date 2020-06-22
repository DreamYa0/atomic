package com.atomic.tools.mock.dto;

import java.util.Map;

public class MockData4Rpc {

    private String rpcMethod;
    private String rpcRequest;
    private Object apiResult;
    private Map<String, String> attachments;

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

    public Object getApiResult() {
        return apiResult;
    }

    public void setApiResult(Object apiResult) {
        this.apiResult = apiResult;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }
}
