package com.atomic.tools.mock.dto;

import com.beust.jcommander.internal.Lists;
import org.apache.dubbo.rpc.RpcResult;

import java.util.List;

public class MockData {
    private List<Object> dbData = Lists.newArrayList();

    private List<RpcResult> rpcData = Lists.newArrayList();

    public List<Object> getDbData() {
        return dbData;
    }

    public void setDbData(List<Object> dbData) {
        this.dbData = dbData;
    }

    public List<RpcResult> getRpcData() {
        return rpcData;
    }

    public void setRpcData(List<RpcResult> rpcData) {
        this.rpcData = rpcData;
    }
}
