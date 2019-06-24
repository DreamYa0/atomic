package com.atomic.tools.mock.dto;

import com.beust.jcommander.internal.Lists;
import org.apache.dubbo.rpc.Result;

import java.util.List;

public class MockDataEncode {
    private List<Object> dbData = Lists.newArrayList();

    private List<Result> rpcData = Lists.newArrayList();

    public List<Object> getDbData() {
        return dbData;
    }

    public void setDbData(List<Object> dbData) {
        this.dbData = dbData;
    }

    public List<Result> getRpcData() {
        return rpcData;
    }

    public void setRpcData(List<Result> rpcData) {
        this.rpcData = rpcData;
    }
}
