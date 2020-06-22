package com.atomic.tools.mock.dto;

import com.google.common.collect.Lists;

import java.util.List;

public class MockData {

    private List<Object> dbData = Lists.newArrayList();
    private List<MockData4Rpc> rpcData = Lists.newArrayList();

    public List<Object> getDbData() {
        return dbData;
    }

    public void setDbData(List<Object> dbData) {
        this.dbData = dbData;
    }

    public List<MockData4Rpc> getRpcData() {
        return rpcData;
    }

    public void setRpcData(List<MockData4Rpc> rpcData) {
        this.rpcData = rpcData;
    }
}
